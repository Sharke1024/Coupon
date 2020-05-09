package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.feign.TemplateClient;
import com.imooc.coupon.service.IRedisService;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h1>用户服务相关接口实现</h1>
 *      所有的操作过程， 状态都是保存到 redis中，并通过Kafka把消息传递到Mysql中
 *      为什么使用 Kafka, 而不是直接使用 SpringBoot 中的异步处理 ?
 *          安全性问题 ，因为异步任务，可能是失败的，如果是springBoot中的异步处理，就会造成丢失，
 *          而使用kafka 即使是失败也可以从消息中进行处理，保证redis中数据的一致性
 * @Author DL_Wu
 * @Date 2020/5/8
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class UserServiceImpl implements IUserService {

    /** Coupon Dao */
    private final CouponDao couponDao;

    /** Redis 服务 */
    private final IRedisService redisService;

    /** 模板微服务客户端 */

    private final TemplateClient templateClient;

    /** 结算微服务客户端 */
    private final SettlementClient settlementClient;

    /** Kafka 客户端 */
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(IRedisService redisService, CouponDao couponDao,
                           TemplateClient templateClient, SettlementClient settlementClient,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.redisService = redisService;
        this.couponDao = couponDao;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * <h2>根据用户 id 和状态查找优惠劵记录</h2>
     * @param userId 用户id
     * @param status 优惠劵状态
     * @return {@link Coupon}s
     * @throws CouponException
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {

        //从redis缓存中获取 优惠劵
        List<Coupon> curCoupon = redisService.getCachedCoupons(userId,status);
        List<Coupon> preTarget;

        if (CollectionUtils.isNotEmpty(curCoupon)){
            log.debug("CurCache Is Not Empty :{},{}" ,userId,status);
            preTarget = curCoupon;
        }else{
            log.debug("CurCaChe Is Empty ,Get Cache form DB :{},{}",userId,status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));

            //如果数据库中没有记录，直接返回就行，Cache中已经加入一张无效优惠劵
            if (CollectionUtils.isEmpty(dbCoupons)){
                log.debug("Cur User do not Hava Coupon:{},{}",userId,status);
                return dbCoupons;
            }

            // 填充 dbCoupons的 templateSDK 字段 , 远程调用template中  获取模板 ids 到 CouponTemplateSDK 的映射
            Map<Integer, CouponTemplateSDK> ids2TemplateSDK = templateClient
                    .findIds2TemplateSDK(dbCoupons.stream().map(Coupon::getTemplateId).collect(Collectors.toList())).getData();
            dbCoupons.forEach(dc -> dc.setTemplateSDK(ids2TemplateSDK.get(dc.getTemplateId())));

            //数据库中存在记录
             preTarget = dbCoupons;
            redisService.addCouponToCache(userId,preTarget,status);
        }

        //将无效优惠劵剔除
        preTarget = preTarget.stream().filter(c -> c.getId() != -1).collect(Collectors.toList());
        // 如果当前获取的是可用优惠劵，还需要做对已过期优惠劵的延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE){
            CouponClassify classify = CouponClassify.classify(preTarget);
            //如果已过期状态不为空，需要做延迟处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())){
                log.info("add expired coupon to cache from findCouponByStatus : {},{}",userId,status);

                //将过期优惠劵加入缓存
                redisService.addCouponToCache(userId,classify.getExpired(),CouponStatus.EXPIRED.getCode());

                //发送到kafka中做异步处理
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                        CouponStatus.EXPIRED.getCode(),
                        classify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList())
                )));
            }
            return classify.getUsable();
        }

        return preTarget;
    }

    /**
     *  <h2>根据用户id 查找当前可用的优惠劵模板</h2>
     * @param userId    用户id
     * @return  {@link CouponTemplateSDK}s
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException{
        long curTime = new Date().getTime();
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();
        log.debug("Find All Template(From TemplateClient) Count :{}",templateSDKS.size());

        //过滤掉过期优惠劵
         templateSDKS = templateSDKS.stream()
                 .filter(t -> t.getRule().getExpiration().getDeadline() > curTime).collect(Collectors.toList());
         log.debug("Find Usable Template Count :{}",templateSDKS.size());

        // key 是 TemplateId
        // value 中的 left 是 Template limitation, right 是优惠券模板
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());
        templateSDKS.forEach(
                t -> limit2Template.put(
                        t.getId(),
                        Pair.of(t.getRule().getLimitation(), t)
                )
        );
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        //找到可用的优惠劵
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());

        log.debug("Current User Has Usable Coupon:{},{}",userId,userUsableCoupons.size());

        // key 是 templateId    , 将List集合转为map集合
        Map<Integer,List<Coupon>> templateId2Coupons =
                userUsableCoupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));

        // 根据 Template 的 Rule 判断是否可以领取优惠券模板
        limit2Template.forEach((k,v) -> {
            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();

            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation)
                return;

            result.add(templateSDK);
        });
        return result;
    }

    /**
     *<h2>用户领取优惠卷</h2>
     *  1.从couponTemplate 拿到对应的优惠劵，并检查是否已过期
     *  2.根据 limitation 判断用户是否可以领取
     *  3.save to DB
     *  4.填充couponTemplateSDK
     *  5.save to Cache
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     * @throws CouponException
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        Map<Integer, CouponTemplateSDK> id2template = templateClient
                .findIds2TemplateSDK(Collections.singletonList(request.getTemplateSDK().getId())).getData();

        //判断优惠劵模板是否存在
        if (id2template.size() <=0){
            log.error("Can Not Acquire Template From TemplateClient :{}",request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template From TemplateClient");
        }

        //用户是否可以领取这张优惠劵
        List<Coupon> userUsableCoupons =findCouponsByStatus(request.getUserId(),CouponStatus.USABLE.getCode());
        Map<Integer ,List<Coupon>> templateId2Coupons = userUsableCoupons
                .stream().collect(Collectors.groupingBy(Coupon::getTemplateId));

        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())  &&
                templateId2Coupons.get(request.getTemplateSDK().getId()).size() >=
                        request.getTemplateSDK().getRule().getLimitation()){
            log.error("Exceed Template Assign Limitation:{}",request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");  // 超过领取限制
        }

        //尝试去获取优惠劵码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(request.getTemplateSDK().getId());
        if (StringUtils.isEmpty(couponCode)){
            log.error("Can Not Acquire Coupon Code:{}",request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }

        Coupon newCoupon = new Coupon(request.getTemplateSDK().getId()
                ,request.getUserId(),couponCode,CouponStatus.USABLE);
        newCoupon = couponDao.save(newCoupon);

        //填充 Coupon 对象的 couponTemplateSDK， 一定要在放入缓冲之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());

        //放入缓存
        redisService.addCouponToCache(request.getUserId(),
                Collections.singletonList(newCoupon),CouponStatus.USABLE.getCode());

        return newCoupon;
    }

    /**
     *<h2>结算(核销)优惠券</h2>
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     * @throws CouponException
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        return null;
    }
}
