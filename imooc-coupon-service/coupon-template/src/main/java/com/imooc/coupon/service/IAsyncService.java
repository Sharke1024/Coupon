package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;

/**
 *  <h1>异步服务接口定义</h1>
 * @Author DL_Wu
 * @Date 2020/4/29 16:27
 * @Version 1.0
 */
public interface IAsyncService {

    /**
     * <h2>根据模板异步的创建优惠券码</h2>
     * @param template {@link CouponTemplate} 优惠券模板实体
     * */
    void asyncConstructCouponByTemplate(CouponTemplate template );

    /**
     * 根据模板 异步删除优惠劵码
     * @param template 模板
     */
    void asyncDeleteCouponTemplateByTemplate(CouponTemplate template);
}
