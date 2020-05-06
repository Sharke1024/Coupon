package com.imooc.coupon.feign.hystrix;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.feign.SettlementClient;
import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * <h1>结算微服务 feign 接口熔断降级策略</h1>
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {

    /**
     * <h2>优惠劵规则计算</h2>
     * @param settlementInfo 结算信息
     * @return  结算信息
     * @throws CouponException
     */
    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlementInfo) throws CouponException {

        log.error("[eureka-client-coupon-settlement] computeRule() request error !!! ");

        settlementInfo.setEmploy(false);  //结算不生效
        settlementInfo.setCost(-1.0);

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-settlement] request error ",
                    settlementInfo
        );
    }
}
