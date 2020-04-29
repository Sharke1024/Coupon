package com.imooc.coupon.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略统一响应注解定义
 * @Author DL_Wu
 * @Date 2020/4/27 15:33
 * @Version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})  //z注解可用于方法，以及类上面
@Retention(RetentionPolicy.RUNTIME)  //在运行时起作用
public @interface IgnoreResponseAdvice {
}
