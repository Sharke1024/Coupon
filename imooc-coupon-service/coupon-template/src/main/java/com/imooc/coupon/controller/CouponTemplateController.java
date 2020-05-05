package com.imooc.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.service.IBuildTemplateService;
import com.imooc.coupon.service.ITemplateBaseService;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *  <h1>优惠券模板相关的功能控制器</h1>
 * @Author DL_Wu
 * @Date 2020/4/30
 */
@Slf4j
@RestController
public class CouponTemplateController {

    /** 构建优惠劵模板 */
    private final IBuildTemplateService buildTemplateService;

    /**优惠劵模板基础服务*/
    private final ITemplateBaseService templateBaseService;

    @Autowired
    public CouponTemplateController(IBuildTemplateService buildTemplateService, ITemplateBaseService templateBaseService) {
        this.buildTemplateService = buildTemplateService;
        this.templateBaseService = templateBaseService;
    }

    /**
     *  <h2>构建优惠劵模板</h2>
     * @param request   模板请求常见对象
     * @return
     *  127.0.0.1:7001/coupon-template/template/build
     *  127.0.0.1:9000/imooc/coupon-template/template/build
     */
    @PostMapping("/template/build")
    public CouponTemplate buildCouponTemplate(@RequestBody TemplateRequest request)throws CouponException {
        log.info("Build Template: {}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    /**
     * <h2>构建优惠劵模板详情</h2>
     *  127.0.0.1:7001/coupon-template/template/build/info?id=19
     *  127.0.0.1:9000/imooc/coupon-template/template/build/info?id=19
     */
    @GetMapping("/template/build/info")
    public CouponTemplate buildCouponTemplateInfo(@RequestParam Integer id) throws CouponException{
        log.info("Build Template Info:{}", id);
        return templateBaseService.buildTemplateInfo(id);
    }

    /**
     * <h2>查找所有可用的优惠券模板</h2>
     * 127.0.0.1:7001/coupon-template/template/sdk/all
     * 127.0.0.1:9000/imooc/coupon-template/template/sdk/all
     */
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> finaAllUsableTemplate() throws CouponException{
        log.info("Find All Usable Template");
        return templateBaseService.finaAllUsableTemplate();
    }

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     * 127.0.0.1:7001/coupon-template/template/sdk/infos
     * 127.0.0.1:9000/imooc/coupon-template/template/sdk/infos
     * */
    @GetMapping("/template/sdk/infos")
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(@RequestParam Collection<Integer> ids){
        log.info("findIds2TemplateSDK :{}",JSON.toJSONString(ids));
        return templateBaseService.findIdsTemplateSDK(ids);
    }


}
