package com.imooc.coupon.executor.impl;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <h1>立减优惠劵规则执行器</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
@Slf4j
@Component
public class LiJianExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * <h1>优惠规则类型标记</h1>
     * @return {@link RuleFlag}
     * */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
    }

    /**
     * <h1>优惠规则的计算</h1>
     * @param settlement{@link SettlementInfo} 包含了选择的优惠劵
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        //获取商品总价
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));

        //判断立减优惠劵是否符合优惠劵限制
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if (null != probability){
            //不符合标准
            log.error("LiJian Template Is Not Match To GoodsType!!!");
            return probability;
        }

        // 立减优惠劵可直接使用 ,无需门槛
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK();
        double quota = (double)templateSDK.getRule().getDiscount().getQuota();

        settlement.setCost(
                retain2Decimals((goodsSum - quota) > minCost() ? (goodsSum- quota ) : minCost())
        );

        log.debug("Use LiJian Coupon Make Goods Cost From {} To {}",goodsSum,settlement.getCost());

        return settlement;

    }
}
