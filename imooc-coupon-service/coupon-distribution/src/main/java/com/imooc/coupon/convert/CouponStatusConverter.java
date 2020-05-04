package com.imooc.coupon.convert;

import com.imooc.coupon.constant.CouponStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <h1>优惠劵状态转换器</h1>  (由于数据库不知道是存储优惠劵状态的 code 还是 description )
 * AttributeConverter<X, Y>
 *      X: 是实体属性的类型
 *      Y: 是数据库字段的类型
 * @Author DL_Wu
 * @Date 2020/5/4
 */
@Converter
public class CouponStatusConverter implements AttributeConverter<CouponStatus,Integer> {

    /**
     * <h2>将实体属性X转换为Y存储到数据库中, 插入和更新时执行的动作</h2>
     * */
    @Override
    public Integer convertToDatabaseColumn(CouponStatus status) {
        return status.getCode();
    }

    /**
     * <h2>将数据库中的字段Y转换为实体属性X, 查询操作时执行的动作</h2>
     * */
    @Override
    public CouponStatus convertToEntityAttribute(Integer code) {
        return  CouponStatus.of(code);
    }
}
