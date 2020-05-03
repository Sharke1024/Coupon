package com.imooc.coupon.service;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.CouponTemplateSDK;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * 模板基础测试
 * @Author DL_Wu
 * @Date 2020/5/3
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TemplateBaseTest {

    @Autowired
    private ITemplateBaseService templateBaseService;

    /**
     * 根据优惠券模板 id 获取优惠券模板信息
     * @throws Exception
     */
    @Test
    public void templateInfo() throws Exception{
        CouponTemplate couponTemplate = templateBaseService.buildTemplateInfo(12);
        //CouponTemplate couponTemplate2 = templateBaseService.buildTemplateInfo(16);
        System.out.println(JSON.toJSONString(couponTemplate));
    }

    /**
     * 查找所有可用的优惠券模板<
     */
    @Test
    public void findAllTemplate(){
        List<CouponTemplateSDK> couponTemplateSDKS = templateBaseService.finaAllUsableTemplate();
        System.out.println(couponTemplateSDKS);
    }

    /**
     * 获取模板 ids 到 CouponTemplateSDK 的映射
     */
    @Test
    public void findIdsTemplateSDK(){
        Map<Integer, CouponTemplateSDK> idsTemplateSDK = templateBaseService.findIdsTemplateSDK(Arrays.asList(11, 12, 13, 10));
        System.out.println(JSON.toJSONString(idsTemplateSDK));
    }

}
