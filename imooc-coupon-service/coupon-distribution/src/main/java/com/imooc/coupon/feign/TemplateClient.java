package com.imooc.coupon.feign;

import com.imooc.coupon.vo.CommonResponse;
import com.imooc.coupon.vo.CouponTemplateSDK;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠劵模板微服务远程调用接口</h1>
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@FeignClient(value = "eureka-client-coupon-template")
public interface TemplateClient {

    /**
     * <h2>查找所有可用的优惠劵模板</h2>
     * @return
     */
    @RequestMapping(value = "/coupon-template/template/sdk/all",method = RequestMethod.GET)
    CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate();

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     * @return
     */
    @RequestMapping(value = "/coupon-template/coupon-template/template/sdk/infos",method = RequestMethod.GET)
    CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(@RequestParam Collection<Integer> ids);

}
