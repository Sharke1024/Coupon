package com.imooc.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.GoodsInfo;
import com.imooc.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  <h1>规则执行器抽象类，定义通用方法</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
public abstract class AbstractExecutor {

    /**
     *  <h2>校验商品类型与优惠劵是否匹配</h2>
     *  需要注意：
     *      1.这里只实现单品类优惠劵的校验，多品类优惠劵重载此方法
     *      2.商品只需要有一个优惠劵要求的商品类型去匹配就可以
     * @param settlement {@link SettlementInfo} 用户传递的结算信息
     */
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement){

        List<Integer> goodsType =  settlement.getGoodsInfos()
                .stream().map(GoodsInfo::getType).collect(Collectors.toList());

        List<Integer> templateGoodsTpye = JSON.parseObject(
                settlement.getCouponAndTemplateInfos().get(0)
                        .getTemplateSDK().getRule().getUsage().getGoodsType()
                ,List.class
        );

        // 判断存在交集即可
        return CollectionUtils.isNotEmpty(
                CollectionUtils.intersection(goodsType,templateGoodsTpye)
        );
    }

    /**
     *  <h2>处理商品类型与优惠券限制不匹配的情况</h2>
     * @param settlement {@link SettlementInfo} 用户传递的结算信息
     * @param goodsSum 商品总价
     * @return {@link SettlementInfo} 已经修改过的结算信息
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlement , double goodsSum){

        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfy(settlement);

        //当商品类型不满足，直接返回总价，并清空优惠劵
        if (! isGoodsTypeSatisfy){
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        return  null;
    }

    /**
     *  商品总价
     * @param goodsInfos {@link GoodsInfo} 商品信息
     * @return
     */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos){
        return goodsInfos.stream().mapToDouble(
                g->g.getPrice() * g.getCount()
        ).sum();
    }

    /**
     *<h2>保留两位小数</h2>
     * @param value 商品价格
     * @return
     */
    protected double retain2Decimals(double value){
        return new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * <h2>最小支付费用</h2>
     * @return
     */
    protected double minCost(){
        return 0.1;
    }

}