package com.imooc.coupon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * jackson的自定义配置
 *  对http序列化的控制
 * @Author DL_Wu
 * @Date 2020/4/27 15:04
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss"
        ));
        return mapper;
    }

}
