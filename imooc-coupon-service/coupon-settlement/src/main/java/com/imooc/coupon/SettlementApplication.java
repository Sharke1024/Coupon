package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@SpringBootApplication
@EnableEurekaClient
public class SettlementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementApplication.class,args);
    }

}
