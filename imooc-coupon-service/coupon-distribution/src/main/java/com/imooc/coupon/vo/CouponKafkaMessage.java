package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>优惠劵 kafka 消息对象定义</h1>
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponKafkaMessage {

    /** 优惠劵状态 */
    private Integer status;

    /** Coupon 主键 */
    private List<Integer> ids;

}
