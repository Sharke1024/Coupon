package com.imooc.coupon.converter;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.vo.TemplateRule;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *<h1>优惠券规则属性转换器</h1>
 * AttributeConverter<X, Y>
 *      X: 是实体属性的类型
 *      Y: 是数据库字段的类型
 * @Author DL_Wu
 * @Date 2020/4/28 19:49
 * @Version 1.0
 */
@Converter
public class RuleConverter implements AttributeConverter<TemplateRule,String> {

    /**
     * <h2>将实体属性X转换为Y存储到数据库中, 插入和更新时执行的动作</h2>
     * */
    @Override
    public String convertToDatabaseColumn(TemplateRule templateRule) {
        return JSON.toJSONString(templateRule);
    }

    /**
     * <h2>将数据库中的字段Y转换为实体属性X, 查询操作时执行的动作</h2>
     * */
    @Override
    public TemplateRule convertToEntityAttribute(String rule) {
        return JSON.parseObject(rule,TemplateRule.class);
    }
}
