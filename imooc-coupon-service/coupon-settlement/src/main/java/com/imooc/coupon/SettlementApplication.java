package com.imooc.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@SpringBootApplication
@EnableEurekaClient
@EnableJpaAuditing
public class SettlementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementApplication.class,args);
    }

}
