package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>fake 商品信息</h1>
 *
 * @Author DL_Wu
 * @Date 2020/5/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsInfo {

    /*** 商品类型 {@link com.imooc.coupon.constant.GoodsType} */
    private Integer type;

    /** 商品价格 */
    private Double price;

    /** 商品数量 */
    private Integer count;

    // TODO 名称， 使用信息

}
