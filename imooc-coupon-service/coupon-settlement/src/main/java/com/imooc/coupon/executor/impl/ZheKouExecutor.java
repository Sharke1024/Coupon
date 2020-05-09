package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * <h1>折扣优惠劵规则结算执行器</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
@Slf4j
@Component
public class ZheKouExecutor extends AbstractExecutor implements RuleExecutor {


    /**
     * <h1>优惠规则类型标记</h1>
     * @return {@link RuleFlag}
     * */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    /**
     * <h1>优惠规则的计算</h1>
     * @param settlement {@link SettlementInfo} 包含了选择的优惠劵
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        //获取商品的总价
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));

        //判断商品类型与优惠劵是否匹配
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement,goodsSum);
        if( null != probability){
            //商品类型与优惠劵不匹配
            log.debug("Goods Type IS Not match To ZheKou template");
            return probability;
        }

        //折扣可以直接使用 ，没有门槛
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK();
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();

        //计算使用优惠劵之后的价格
        settlement.setCost(
                retain2Decimals(goodsSum * (quota * 1.0 /100)) > minCost() ?
                        retain2Decimals(goodsSum * (quota * 1.0 /100)) :minCost()
        );

        log.debug("Use ZheKou Coupon Make Goods Cost From {} To {}",goodsSum,settlement.getCost());
        return settlement;
    }
}
