package com.imooc.coupon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 *  <h1>定制http消息转换器</h1>
 * @Author DL_Wu
 * @Date 2020/4/27 14:57
 * @Version 1.0
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //清除原有的信息
        converters.clear();
        //java对象转json
        converters.add(new MappingJackson2CborHttpMessageConverter());
    }
}
