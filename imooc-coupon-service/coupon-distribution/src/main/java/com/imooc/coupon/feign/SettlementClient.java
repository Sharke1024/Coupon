package com.imooc.coupon.feign;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.hystrix.SettlementClientHystrix;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *  <h1>优惠券结算微服务 Feign 接口定义</h1>
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@FeignClient(value = "eureka-client-coupon-settlement",fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    /**
     * <h2>优惠劵规则计算</h2>
     * @param settlementInfo 结算信息
     * @return  结算信息
     * @throws CouponException
     */
    @RequestMapping(value = "/coupon-settlement/settlement/compute",method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(@RequestBody SettlementInfo settlementInfo) throws CouponException;

}
