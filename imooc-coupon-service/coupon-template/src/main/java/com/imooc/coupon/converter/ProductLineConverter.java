package com.imooc.coupon.converter;

import com.imooc.coupon.constant.ProductLine;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <h1>产品线枚举属性转换器</h1> (由于数据库不知道是存储 产品线 的 code 还是 description )
 * AttributeConverter<X, Y>
 *      X: 是实体属性的类型
 *      Y: 是数据库字段的类型
 * @Author DL_Wu
 * @Date 2020/4/28 19:39
 * @Version 1.0
 */
@Converter
public class ProductLineConverter implements AttributeConverter<ProductLine, Integer> {

    /**
     * <h2>将实体属性X转换为Y存储到数据库中, 插入和更新时执行的动作</h2>
     * */
    @Override
    public Integer convertToDatabaseColumn(ProductLine productLine) {
        return productLine.getCode();
    }

    /**
     * <h2>将数据库中的字段Y转换为实体属性X, 查询操作时执行的动作</h2>
     * */
    @Override
    public ProductLine convertToEntityAttribute(Integer code) {
        return ProductLine.of(code);
    }
}
