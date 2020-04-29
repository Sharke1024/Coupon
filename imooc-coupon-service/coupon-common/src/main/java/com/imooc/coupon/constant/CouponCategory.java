package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h1>优惠券分类</h1>
 * @Author DL_Wu
 * @Date 2020/4/28 15:20
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CouponCategory {

    MANJIAN("满减券", "001"),  //manjian
    ZHEKOU("折扣券", "002"),   //zhekou
    LIJIAN("立减券", "003");   //lijian

    /** 优惠券描述(分类) */
    private String description;

    /** 优惠券分类编码 */
    private String code;

    public static CouponCategory of(String code){
        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny().orElseThrow(() -> new IllegalArgumentException(code + "not exists !"));
    }

}
