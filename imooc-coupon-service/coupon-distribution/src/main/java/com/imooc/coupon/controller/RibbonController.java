package com.imooc.coupon.controller;

import com.imooc.coupon.annotation.IgnoreResponseAdvice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 *  Ribbon 应用
 * @Author DL_Wu
 * @Date 2020/5/11
 */
@Slf4j
@RestController
public class RibbonController {

    private final RestTemplate restTemplate;

    @Autowired
    public RibbonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    /**
     * <h2>通过Ribbon 组件调用 模板Template 微服务</h2>
     * @return
     */
    @GetMapping("/template/info")
    @IgnoreResponseAdvice
    public TemplateInfo getTemplateInfo(){
        String infoUrl ="http://eureka-client-coupon-template/coupon-template/info";
        return restTemplate.getForEntity(infoUrl,TemplateInfo.class).getBody();
    }

    /**
     * 通过Ribbon 组件调用 结算Settlement 微服务
     * <h2></h2>
     * @return
     */
    @GetMapping("/settlement/info")
    @IgnoreResponseAdvice
    public SettlementInfo  getSettlementInfo(){
        String infoUrl ="http://eureka-client-coupon-settlement/coupon-settlement/info";
        return restTemplate.getForEntity(infoUrl,SettlementInfo.class).getBody();
    }

    /**
     *<h2>模板微服务模板的元信息</h2>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TemplateInfo{
        private Integer code;
        private String message;
        private List<Map<String ,Object>> data;
    }

    /**
     *<h2>结算微服务模板的元信息</h2>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SettlementInfo{
        private Integer code;
        private String message;
        private List<Map<String ,Object>> data;
    }
}
