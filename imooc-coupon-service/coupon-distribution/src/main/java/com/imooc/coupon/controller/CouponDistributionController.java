package com.imooc.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IUserService;
import com.imooc.coupon.vo.AcquireTemplateRequest;
import com.imooc.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h1>优惠劵分发相关功能控制器</h1>
 *
 * @Author DL_Wu
 * @Date 2020/5/8
 */
@RestController
@Slf4j
public class CouponDistributionController {

    private final IUserService userService;

    @Autowired
    public CouponDistributionController(IUserService userService) {
        this.userService = userService;
    }

    /**
     * <h2>根据用户id和 状态 查询优惠劵记录</h2>
     * @param userId 用户id
     * @param status 当前状态
     * @return
     * @throws CouponException
     *
     * 127.0.0.1:7002/coupon-distribution/distribution/find/coupons?userId=2222&status=3
     * 127.0.0.1：9000/imooc/coupon-distribution/distribution/find/coupons?userId=2222&status=3
     */
    @GetMapping("/distribution/find/coupons")
    public List<Coupon> findCouponsByStatus(@RequestParam Long userId, @RequestParam Integer status)
            throws CouponException{
        log.info("Find Coupons By UserId And Status,{},{}",userId,status);
        return userService.findCouponsByStatus(userId,status);
    }

    /**
     * <h2>根据用户 id 查找当前可用的优惠劵模板</h2>
     * @param userId 用户id
     * @return
     * @throws CouponException
     *
     * 127.0.0.1:7002/coupon-distribution/distribution/find/template?userId=2222
     * 127.0.0.1：9000/imooc/coupon-distribution/distribution/find/template?userId=2222
     */
    @GetMapping("distribution/find/template")
    public List<CouponTemplateSDK> findAvailableTemplate(@RequestParam Long userId) throws CouponException{
        log.info("Find Available Template By UserId :{}",userId);
        return userService.findAvailableTemplate(userId);
    }

    /**
     *  <h2>用户领取优惠劵</h2>
     * @param request request {@link AcquireTemplateRequest} 优惠劵请求对象
     * @return
     *
     * 127.0.0.1:7002/coupon-distribution/distribution/acquire/template
     * 127.0.0.1：9000/imooc/coupon-distribution/distribution/acquire/template
     */
    @PostMapping("/distribution/acquire/template")
    public Coupon acquireTemplate(@RequestBody AcquireTemplateRequest request)throws CouponException{
        log.info("User Get Template Coupon Code:{}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }

}
