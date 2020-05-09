package com.imooc.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>满减 + 折扣优惠劵规则执行器</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
@Slf4j
@Component
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * <h1>优惠规则类型标记</h1>
     * @return {@link RuleFlag}
     * */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    /**
     *  <h2>校验商品类型与优惠劵是否匹配</h2>
     *  需要注意：
     *      1. 这里实现的满减 + 折扣优惠券的校验
     *      2. 如果想要使用多类优惠券, 则必须要所有的商品类型都包含在内, 即差集为空
     * @param settlement {@link SettlementInfo} 用户传递的结算信息
     */
    @Override
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        log.debug("Check ManJian And Zhekou Is Match Or Not");
        List<Integer> goodsType = settlement.getGoodsInfos()
                .stream().map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType =new ArrayList<>();

        settlement.getCouponAndTemplateInfos().forEach(
            ct ->{
                templateGoodsType.addAll(
                        JSON.parseObject(
                                ct.getTemplateSDK().getRule().getUsage().getGoodsType(),
                                List.class
                        )
                );
            }
        );

        // 如果想要使用多类优惠劵，则必须要所有的商品类型都包含在内，即差集为空
        return CollectionUtils.isEmpty(CollectionUtils.subtract(
                goodsType,templateGoodsType
        ));
    }

    /**
     * <h1>优惠规则的计算</h1>
     * @param settlement{@link SettlementInfo} 包含了选择的优惠劵
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));

        return null;
    }
}
