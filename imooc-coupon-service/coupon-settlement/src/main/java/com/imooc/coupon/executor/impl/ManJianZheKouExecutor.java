package com.imooc.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.executor.AbstractExecutor;
import com.imooc.coupon.executor.RuleExecutor;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

        //商品类型的校验
        SettlementInfo probability =processGoodsTypeNotSatisfy(settlement,goodsSum);
        if (null != probability){
            log.debug("ManJian And ZheKou Template Is Not Match To GoodsType!");
            return probability;
        }

        SettlementInfo.CouponAndTemplateInfo manjian = null;
        SettlementInfo.CouponAndTemplateInfo zhekou = null;

        for (SettlementInfo.CouponAndTemplateInfo ct : settlement.getCouponAndTemplateInfos()){
            if (CouponCategory.of(ct.getTemplateSDK().getCategory()) == CouponCategory.MANJIAN){
                manjian =ct ;
            }else{
                zhekou = ct;
            }
        }

        //断言
        assert null != manjian;
        assert null != zhekou;

        // 当前的折扣优惠券和满减券如果不能共用(一起使用), 清空优惠券, 返回商品原价
        if( ! isTemplateCanShared(manjian,zhekou)){
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double manjianBase = manjian.getTemplateSDK().getRule().getDiscount().getBase();
        double manjianQuota = manjian.getTemplateSDK().getRule().getDiscount().getQuota();

        //最终价格
        double targetSum = goodsSum;

        //先计算满满减
        if (targetSum >= manjianBase){
            targetSum = targetSum- manjianQuota;
            ctInfos.add(manjian);
        }

        //再计算折扣
        double zhekouQuota = zhekou.getTemplateSDK().getRule().getDiscount().getQuota();
        targetSum *= zhekouQuota * 1.0 /100;
        ctInfos.add(zhekou);

        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(retain2Decimals(
                targetSum > minCost() ? targetSum : minCost()
        ));

        log.debug("Use Manjian And ZheKOU Coupon Make Goods Cost from {} To {} ",goodsSum,settlement.getCost());

        return settlement;
    }


    /**
     *  <h1>当前两张优惠劵能否一起使用</h1>
     *      即校验TemplateRule 中的weight 是否满足条件
     * @param manjian   满减优惠劵
     * @param zhekou    折扣优惠劵
     * @return
     */
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manjian,
                                        SettlementInfo.CouponAndTemplateInfo zhekou) {
        //获取满减以及折扣的 Key
        String manjianKey = manjian.getTemplateSDK().getKey()+String.format("%04d",manjian.getTemplateSDK().getId());
        String zhekouKey = zhekou.getTemplateSDK().getKey()+ String.format("%04d",zhekou.getTemplateSDK().getId());

        List<String> allSharedKeysForManjian= new ArrayList<>();
        allSharedKeysForManjian.addAll(
                JSON.parseObject(
                    manjian.getTemplateSDK().getRule().getWeight(),
                        List.class
                )
        );

        List<String> allSharedKeysForZheKou = new ArrayList<>();
        allSharedKeysForZheKou.addAll(
                JSON.parseObject(zhekou.getTemplateSDK().getRule().getWeight(),
                        List.class
                )
        );

        return CollectionUtils.isSubCollection(
                        Arrays.asList(manjianKey,zhekouKey),allSharedKeysForManjian) ||
                CollectionUtils.isSubCollection(
                        Arrays.asList(manjianKey,zhekouKey),allSharedKeysForZheKou);
    }
}
