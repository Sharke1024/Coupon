package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author DL_Wu
 * @Date 2020/5/5
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {


    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * <h2>根据 userId 和 状态 找到缓存的优惠券列表数据</h2>
     * @param userId 用户 id
     * @param status 优惠券状态 {@link com.imooc.coupon.constant.CouponStatus}
     * @return {@link Coupon}s, 注意, 可能会返回 null, 代表从没有过记录
     */
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons Form Cache : {},{}",userId,status);
        String redisKey = status2RedisKey(status,userId);

        List<String >couponStrs =redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o-> Objects.toString(o,null))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(couponStrs)){
            saveEmptyCouponListToCache(userId,Collections.singletonList(status));
            return Collections.emptyList();
        }

        return couponStrs.stream().map(cs -> JSON.parseObject(cs , Coupon.class)).collect(Collectors.toList());
    }

    /**
     * <h2>保存空的优惠劵到缓存中</h2>
     *      目的：
     *          避免缓存穿透问题
     *
     * @param userId 用户id
     * @param status 优惠劵状态列表
     */
    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save Empty List TO Cache For User :{} , Status: {}",userId,status);

        // key 是 coupon_id, value 是序列化的 Coupon
        Map<String ,String> invalidCouponMap =new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));

        //用户优惠劵缓存信息
        //K　Ｖ
        //K :status -> redisKey
        //V:{Coupon_id : 序列化的coupon}

        //使用 SessionCallback 把数据命令放入到 Redis 的 pipeline
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations  operations) throws DataAccessException {
                status.forEach( s -> {
                    String redisKey = status2RedisKey(s , userId);
                    operations.opsForHash().putAll(redisKey,invalidCouponMap);
                });
                return null;
            }
        };

        log.info("PipeLine Exe Result :{}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }


    /**
     * <h2>尝试从 Cache 中获取一个优惠劵码</h2>
     * @param templateId    优惠卷模板主键
     * @return  优惠劵码
     */
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {

        String redisKey = String.format("%s%s",Constant.redisPrefix.USER_COUPON_USABLE,templateId.toString());

        // 因为优惠券码不存在顺序关系, 左边 pop 或右边 pop, 没有影响
        String couponCode = redisTemplate.opsForList().rightPop(redisKey);

        log.info("Acquire Coupon Code :{},{},{}",templateId,redisKey,couponCode);

        return couponCode;
    }

    /**
     *  <h2>将优惠劵保存到Cache中</h2>
     * @param userId 用户id
     * @param coupons {@Link Coupon}s
     * @param status   优惠卷状态
     * @return  成功保存的个数
     * @throws CouponException
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        log.info("Add Coupon TO Cache :{},{},{}",userId,JSON.toJSONString(coupons),status);

        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus){
            case USABLE:
                result = addCouponToCacheUsable(userId,coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheExpired(userId,coupons);
                break;
            case USED:
                result = addCouponToCacheUsed(userId,coupons);
                break;
        }
        return result;
    }

    /**
     * <h2>将已使用的优惠劵添加到缓存中</h2>
     * @param userId 用户id
     * @param coupons
     * @return
     */
    @SuppressWarnings("all")
    private Integer addCouponToCacheUsed(Long userId, List<Coupon> coupons) throws CouponException{
        //如果 status 是USED 代表用户操作的是当前优惠劵，影响到两个cache
        //USABLE . UESD
        log.debug("Add Coupon To Cache For Used .");
        Map<String ,String> needCacheForUsed = new HashMap<>(coupons.size());


        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(),userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(),userId);

        // 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());

        // 当前可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> needCacheForUsed.put(c.getId().toString(),JSON.toJSONString(c)));

        // 校验当前的优惠券参数是否与 Cached 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramsIds = coupons.stream().map(Coupon::getId).collect(Collectors.toList());

        if ( ! CollectionUtils.isSubCollection(paramsIds,curUsableIds)){
            //参数不匹配
            log.error("CurCoupons Is Not Equal ToCaChe:{},{},{}",
                    userId,JSON.toJSONString(paramsIds),JSON.toJSONString(curUsableIds));
            throw new CouponException("CurCoupons Is Not Equal ToCaChe");
        }

        List<String> needCleanKey = paramsIds.stream().map(i -> i.toString()).collect(Collectors.toList());

        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations operations) throws DataAccessException {

                //1.已使用的优惠劵 Cache 缓存添加
                operations.opsForHash().putAll(redisKeyForUsed,needCacheForUsed);

                //2.可用优惠劵 Cache 需要清理
                operations.opsForHash().delete(redisKeyForUsable,needCleanKey);

                //3.重置过期时间
                operations.expire(
                        redisKeyForUsed,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );
                operations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );
                return null;
            }
        };

        log.info("Pipeline Exe Result:{}",JSON.toJSONString(
            redisTemplate.executePipelined(sessionCallback)
        ));

        return coupons.size();
    }


    /**
     * <h2>将已过期的优惠劵添加到缓存中</h2>
     * @param userId 用户id
     * @param coupons 优惠劵实体表
     * @return
     */
    private Integer addCouponToCacheExpired(Long userId, List<Coupon> coupons) throws CouponException{
        // status 是 EXPIRED, 代表是已有的优惠券过期了, 影响到两个 Cache
        // USABLE, EXPIRED
        log.debug("Add Coupon To Cache For Expired ");

        Map<String ,String > needCachedForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(),userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(),userId);

        // 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());

        //当前可用优惠劵数量 > 1
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach(c-> needCachedForExpired.put(c.getId().toString(),JSON.toJSONString(c)));

        // 校验当前的优惠券参数是否与 Cached 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
        List<Integer> params = coupons.stream().map(Coupon::getId).collect(Collectors.toList());
        if ( ! CollectionUtils.isSubCollection(params,curUsableCoupons)){
            log.error("CurCoupon Is Not Equal To Cache:{},{},{}",
                    userId,JSON.toJSONString(params),JSON.toJSONString(curUsableCoupons));
            throw new CouponException("CurCoupon Is Not Equal To Cache");
        }

        //获得需要清理的key
        List<String> needCleanKey = params.stream().map(i -> i.toString()).collect(Collectors.toList());

        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public  Objects execute(RedisOperations operations) throws DataAccessException {
                //1.可用的优惠券 Cache 需要清理
                operations.opsForHash().delete(redisKeyForUsable,needCleanKey.toArray());

                // 2.已过期的优惠券 Cache 缓存
                operations.opsForHash().putAll(redisKeyForExpired,needCachedForExpired);

                //3.设置过期时间
                operations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );
                operations.expire(
                        redisKeyForExpired,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );
                return null;
            }
        } ;
        log.info("PipeLine Exc Result: {}",JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));

        return coupons.size();
    }

    /**
     * <h2>添加一张优惠劵到缓存中</h2>
     * @param userId 用户id
     * @param coupons 优惠劵实体表
     * @return
     */
    private Integer addCouponToCacheUsable(Long userId, List<Coupon> coupons) {
        //如果status 是Usable ，代表是新增加的优惠劵
        // 只会影响一个 Cache：User_Coupon_Usable
        log.debug("Add Coupon To Cache For Usable");
        Map<String,String > needCachedObject = new HashMap<>();
        coupons.forEach(c -> needCachedObject.put(c.getId().toString(),JSON.toJSONString(c)));

        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(),userId);
        //加入redis缓存中
        redisTemplate.opsForHash().putAll(redisKey,needCachedObject);

        log.info("Add {} Coupons To Cache: {}, {}",
                needCachedObject.size(), userId, redisKey);

        //设置过期时间
        redisTemplate.expire(redisKey,getRandomExpirationTime(1,2), TimeUnit.SECONDS);

        return needCachedObject.size();
    }


    /*
     * <h2>根据 status 获取到对应的 Redis Key</h2>
     * @param status    状态
     * @param userId    用户id
     * @return
     */
    private String status2RedisKey(Integer status, Long userId) {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus){
            case USABLE:
                redisKey = String.format("%s%s", Constant.redisPrefix.USER_COUPON_USABLE,userId);
                break;
            case USED:
                redisKey = String.format("%s%s",Constant.redisPrefix.USER_COUPON_USED,userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s",Constant.redisPrefix.USER_COUPON_EXPIRED,userId);
                break;
        }
        return redisKey;
    }

    /**
     *<h2>获取一个随机的过期时间</h2>
     *  防止缓存雪崩 ： key 在同一时间过期
     * @param min   最小的小时数
     * @param max   最大的小时数
     * @return  返回一个[min, max] 之间的随机秒数
     */
    private Long getRandomExpirationTime(Integer min, Integer max){
        return RandomUtils.nextLong(
                min * 60 * 60,
                max * 60 * 60
        );
    }
}
