package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * <h1>规则类型枚举定义</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
@Getter
@AllArgsConstructor
public enum  RuleFlag {

    //单类优惠劵计算规则
    MANJIAN("满减劵的计算规则"),
    LIJIAN("立减劵的计算规则"),
    ZHEKOU("折扣劵的计算规则"),

    //多类别优惠劵计算
    MANJIAN_ZHEKOU("满减劵 + 折扣劵计算规则");

    // TODO 更多优惠券类别的组合

    /** 规则描述 */
    private String description;




}
