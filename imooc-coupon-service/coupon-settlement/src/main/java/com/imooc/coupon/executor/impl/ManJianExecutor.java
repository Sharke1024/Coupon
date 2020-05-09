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
 * <h1>满减优惠劵结算规则执行器</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
@Slf4j
@Component
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * <h1>优惠规则类型标记</h1>
     * @return {@link RuleFlag}
     * */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
    }

    /**
     * <h1>优惠规则的计算</h1>
     * @param settlement {@link SettlementInfo} 包含了选择的优惠劵
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        //获取商品总价
        Double goodsSum = retain2Decimals(
                goodsCostSum(settlement.getGoodsInfos())
        );

        //处理商品类型与优惠券限制不匹配的情况
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if (null != probability){
            //不等于空则不匹配
            log.debug("Manjian Template Is Not Match To GoodsType!!!");
            return probability;
        }

        //判断满减是否符合折扣标准
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK();
        double base = templateSDK.getRule().getDiscount().getBase();   //满多少才能用
        double quota = templateSDK.getRule().getDiscount().getQuota(); //额度

        //如果不符合标准，则直接返回商品总价
        if (base < quota){
            log.debug("Current Goods Sum < ManJian Coupon Base");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        //计算使用满减之后的价格 - 结算
        settlement.setCost(retain2Decimals(
                (goodsSum - quota) > minCost() ? (goodsSum - quota) : minCost()
        ));

        log.debug("Use Manjian Coupon Make Goods Cost From {} To {}",goodsSum,settlement.getCost());

        return settlement;
    }
}
