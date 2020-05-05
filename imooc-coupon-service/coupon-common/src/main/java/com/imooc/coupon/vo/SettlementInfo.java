package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>结算信息对象定义</h1>
 * 包含：
 *  1.userId
 *  2.商品信息（列表）
 *  3.优惠劵列表
 *  4.结算结果金额
 * @Author DL_Wu
 * @Date 2020/5/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {

    /** 用户id */
    private Long userId;

    /** 商品信息 */
    private List<GoodsInfo> goodsInfos;

    /** 优惠劵列表 */
    private List<CouponAndTemplateInfo > couponAndTemplateInfos;

    /** 是否结算生效 ，即核销 */
    private Boolean employ;

    /** 结果结算金额 */
    private Double cost;

    /**
     * 优惠劵和模板信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public  static class  CouponAndTemplateInfo{

        /**  coupon主键 */
        private Integer id;

        /** 优惠劵对应的模板对象 */
        private CouponTemplateSDK templateSDK;

    }
}
