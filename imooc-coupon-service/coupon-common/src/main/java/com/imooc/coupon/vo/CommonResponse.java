package com.imooc.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <h1>通用响应对象定义</h1>
 * @Author DL_Wu
 * @Date 2020/4/27 15:34
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> implements Serializable {

    private Integer code;

    private String message;

    private T data;

    public CommonResponse(Integer code,String message){
        this.code = code;
        this.message = message;
    }

}
