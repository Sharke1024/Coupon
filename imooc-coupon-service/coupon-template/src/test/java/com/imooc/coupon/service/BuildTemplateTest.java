package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.DistributeTarget;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.constant.ProductLine;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.TemplateRequest;
import com.imooc.coupon.vo.TemplateRule;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.unit.DataUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * 构建优惠券模板接口测试
 * @Author DL_Wu
 * @Date 2020/5/3
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@SuppressWarnings("all")
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void buildTemplateTest() throws Exception{

        CouponTemplate couponTemplate = buildTemplateService.buildTemplate(fakeBuildTemplateRequest());
        System.out.println(JSON.toJSONString(couponTemplate));

        //因为需要异步构建模板
        Thread.sleep(5000);
    }

    private TemplateRequest fakeBuildTemplateRequest(){
        TemplateRequest request = new TemplateRequest();
        request.setName("imooc_template_"+new Date().getTime());
        request.setLogo("www.vip222.com");
        request.setCategory(CouponCategory.MANJIAN.getCode());
        request.setProductLine(ProductLine.DAMAO.getCode());
        request.setCount(10000);
        request.setDesc("优惠劵");
        request.setTarget(DistributeTarget.MULTI.getCode());
        request.setUserId(2222l);

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(20,100));
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(),1, DateUtils.addDays(new Date(),60).getTime()));//有效期限规则
        rule.setLimitation(1); //每个人最多领几张限制
        rule.setUsage(new TemplateRule.Usage(
                "湖南","岳阳", JSON.toJSONString(Arrays.asList("文娱","生鲜"))));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));

        request.setRule(rule);
        return request;
    }




}
