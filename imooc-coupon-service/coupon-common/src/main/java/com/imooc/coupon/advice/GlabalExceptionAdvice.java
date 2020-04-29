package com.imooc.coupon.advice;

import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 * @Author DL_Wu
 * @Date 2020/4/27 16:33
 * @Version 1.0
 */
@RestControllerAdvice
public class GlabalExceptionAdvice {


    /**
     * 对 CouponException 进行统一处理
     * @param request   http请求
     * @param ex    项目通用异常
     * @return  响应信息
     *
     * 优化：定义不同类型的异常枚举（异常码和异常信息）
     */
    @ExceptionHandler(value = CouponException.class)  //可以对指定的异常进行拦截
    public CommonResponse<String> handlerCouponExcetion(HttpServletRequest request, CouponException ex){
        CommonResponse<String> response = new CommonResponse<>(-1,"business Error");
        response.setData(ex.getMessage());
        return response;
    }

}
