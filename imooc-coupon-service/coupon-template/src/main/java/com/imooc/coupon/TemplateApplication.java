package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author DL_Wu
 * @Date 2020/4/28 14:59
 * @Version 1.0
 */
@EnableScheduling   //定时任务
@EnableJpaAuditing     //jpa自动注入
@EnableEurekaClient     //eureka客户端
@SpringBootApplication
public class TemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class,args);
    }

}
