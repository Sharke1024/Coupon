package com.imooc.coupon.service;

import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * <h1>用户服务相关的接口定义</h1>
 * 1. 用户三类状态优惠券信息展示服务
 * 2. 查看用户当前可以领取的优惠券模板 - coupon-template 微服务配合实现
 * 3. 用户领取优惠券服务
 * 4. 用户消费优惠券服务 - coupon-settlement 微服务配合实现
 *
 * @Author DL_Wu
 * @Date 2020/5/4
 */
public interface IUserService {

    /**
     * <h2>根据用户id和状态 查询优惠劵记录</h2>
     * @param userId 用户id
     * @param status 优惠劵状态
     * @return {@link Coupon}s
     * @throws CouponException
     */
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;

    /**
     *  <h2>根据用户id 查找当前可用的优惠劵模板</h2>
     * @param userId    用户id
     * @return  {@link CouponTemplateSDK}s
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     *<h2>用户领取优惠卷</h2>
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     * @throws CouponException
     */
    Coupon acquireTemplate(AcquireTemplateRequest request)throws CouponException;

    /**
     *<h2>结算(核销)优惠券</h2>
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     * @throws CouponException
     */
    SettlementInfo settlement(SettlementInfo info)throws CouponException;

}
