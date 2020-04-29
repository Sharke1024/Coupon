package com.imooc.coupon.service.impl;

import com.google.common.base.Stopwatch;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *<h1>异步服务接口实现</h1>
 * @Author DL_Wu
 * @Date 2020/4/29
 */
@Service
@Slf4j
public class AsyncServiceImpl implements IAsyncService {

    private final StringRedisTemplate redisTemplate  ;

    private final CouponTemplateDao couponTemplateDao;

    @Autowired
    public AsyncServiceImpl(StringRedisTemplate redisTemplate,CouponTemplateDao couponTemplateDao) {
        this.redisTemplate = redisTemplate;
        this.couponTemplateDao = couponTemplateDao;
    }

    /**
     * <h2>根据模板异步的创建优惠券码</h2>
     * @param template {@link CouponTemplate} 优惠券模板实体
     * */
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Stopwatch watch =Stopwatch.createStarted();

        Set<String >couponCodes = buildCouponCode(template);

        // imooc_coupon_template_code_1
        String redisKey =String.format("%s%s", Constant.redisPrefix.COUPON_TEMPLATE,
                template.getId().toString());
        log.info("push Coupon to Redis : {}" ,
                redisTemplate.opsForList().rightPushAll(redisKey,couponCodes));

        //设置状态为可用
        template.setAvailable(true);
        couponTemplateDao.save(template);

        watch.stop();
        log.info("Construct CouponCode By Template Cost: {}ms" , watch.elapsed(TimeUnit.MILLISECONDS));

        //TODO  发送短信或者邮件通知优惠券模板已经可用
        log.info("CouponTemplate({}) Is Available!", template.getId());
    }

    /**
     * <h2>构造优惠卷码</h2>
     * 优惠劵码（对应于每一张优惠劵，18位）
     * 前四位：产品线+ 类型
     * 中六位：日期随机（200429）
     * 后八位：0~9随机8位数
     *
     * @param template {@link CouponTemplate}实体类
     * @return Set<String> 与 template.count 相同个数的优惠券码
     */
    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();

        Set<String> results = new HashSet<>(template.getCount());  //指定初始容量，减少扩容

        //前四位
        String prefix4 =template.getProductLine().getCode().toString()
                + template.getCategory().toString();
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreateTime());


        for (int i = 0; i < template.getCount(); i++) {
            results.add(prefix4 + buildCouponCodeSuffix14(date));
        }
        //之所以在这使用while重新 add() 进 result ，是 HashSet() 会自动排除重复得key ，
        // 而前面先使用for循环是为了提升创建速度，使用while每次都要判断results的长度是否符合要求
        while (results.size() < template.getCount()){
            results.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        //断言是否相等
        assert results.size() ==template.getCount();

        watch.stop();
        log.info("Build Coupon code cost :{}ms" , watch.elapsed(TimeUnit.MILLISECONDS));

        return results;
    }

    /**
     *<h2>构造优惠券码的后 14 位</h2>
     * @param date 创建优惠券的日期
     * @return 14 位优惠券码
     */
    public String buildCouponCodeSuffix14(String date){
        char[] bases = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};

        //中间六位数
        List<Character> chars =date.chars().mapToObj(e -> (char)e).collect(Collectors.toList());

        Collections.shuffle(chars);

        String mid6 =chars.stream().map(Object :: toString).collect(Collectors.joining());

        //后八位
        String  suffix8 = RandomStringUtils.random(1,bases) +RandomStringUtils.random(7);
        return mid6 + suffix8;
    }


}
