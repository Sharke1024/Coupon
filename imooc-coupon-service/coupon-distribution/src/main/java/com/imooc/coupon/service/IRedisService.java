package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;

import java.util.List;

/**
 * <h1>Redis 相关的操作服务接口定义</h1>
 * 1. 用户的三个状态优惠券 Cache 相关操作
 * 2. 优惠券模板生成的优惠券码 Cache 操作
 * @Author DL_Wu
 * @Date 2020/5/4
 */
public interface IRedisService {

    /**
     * <h2>根据 userId 和 状态 找到缓存的优惠券列表数据</h2>
     * @param userId 用户 id
     * @param status 优惠券状态 {@link com.imooc.coupon.constant.CouponStatus}
     * @return {@link Coupon}s, 注意, 可能会返回 null, 代表从没有过记录
     */
    List<Coupon> getCachedCoupons(Long userId, Integer status);

    /**
     * <h2>保存空的优惠劵到缓存中</h2>
     *      目的：
     *          避免缓存穿透问题
     *
     * @param userId 用户id
     * @param status 优惠劵状态列表
     */
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);

    /**
     * <h2>尝试从 Cache 中获取一个优惠劵码</h2>
     * @param templateId    优惠卷模板主键
     * @return  优惠劵码
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     *  <h2>将优惠劵保存到Cache中</h2>
     * @param userId 用户id
     * @param coupons {@Link Coupon}s
     * @param status   优惠卷状态
     * @return  成功保存的个数
     * @throws CouponException
     */
    Integer addCouponToCache(Long userId,List<Coupon> coupons,Integer status) throws CouponException;

}
