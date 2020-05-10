package com.imooc.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.executor.ExecutorManager;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>结算服务 controller</h1>
 * @Author DL_Wu
 * @Date 2020/5/10
 */
@Slf4j
@RestController
public class SettlementController {

    /** 结算规则执行管理器 */
    private final ExecutorManager executorManager;

    @Autowired
    public SettlementController(ExecutorManager executorManager) {
        this.executorManager = executorManager;
    }

    /**
     * <h2>优惠劵结算</h2>
     * @param settlement 结算前信息
     * @return 结算后的信息
     * @throws CouponException
     * 127.0.0.1:7003/coupon-settlement/settlement/compute
     * 127.0.0.1:9000/imooc/coupon-settlement/settlement/compute
     */
    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlement) throws CouponException{
        log.info("settlement:{}", JSON.toJSONString(settlement));
        return executorManager.computeRule(settlement);
    }
}
