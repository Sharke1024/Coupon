package com.imooc.coupon.service;

import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠券模板基础(view, delete...)服务定义</h1>
 * @Author DL_Wu
 * @Date 2020/4/29 17:03
 * @Version 1.0
 */
public interface ITemplateBaseService {

    /**
     *  <h2>根据优惠券模板 id 获取优惠券模板信息</h2>
     * @param id 模板id
     * @return  {@link CouponTemplate} 优惠券模板实体
     * @throws CouponException
     */
    CouponTemplate buildTemplateInfo(Integer id)throws CouponException;

    /**
     *  <h2>查找所有可用的优惠券模板</h2>
     * @return  {@link CouponTemplateSDK}s
     */
    List<CouponTemplateSDK> finaAllUsableTemplate();

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     * @param ids 模板 ids
     * @return Map<key: 模板 id， value: CouponTemplateSDK>
     * */
    Map<Integer , CouponTemplateSDK> findIdsTemplateSDK(Collection<Integer> ids);

    /**
     * 根据模板id 删除对应模板
     * @param id TemplateId
     */
    void deleteByTemplateId(Integer id) throws CouponException;

}
