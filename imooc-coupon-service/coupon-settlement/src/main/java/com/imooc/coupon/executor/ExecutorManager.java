package com.imooc.coupon.executor;

import com.imooc.coupon.constant.CouponCategory;
import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.exception.CouponException;
import com.imooc.coupon.vo.CouponTemplateSDK;
import com.imooc.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠劵结算规则执行管理器</h1>
 *      根据用户的请求（SettlementInfo） 找到对应的Executor ，去做结算
 *      BeanPostProcessor: Bean 后置处理器
 * @Author DL_Wu
 * @Date 2020/5/10
 */
@Slf4j
@Component
public class ExecutorManager implements BeanPostProcessor {

    /** 规则执行器映射 */
    private static Map<RuleFlag,RuleExecutor> executorIndex = new HashMap<>(RuleFlag.values().length);

    /**
     *  <h2>优惠劵结算规则计算入口</h2>
     *      注意；一定要保证传递进来的优惠劵个数 >=1
     */
    public SettlementInfo computeRule(SettlementInfo settlement) throws CouponException{
        SettlementInfo result = null;

        //单类优惠劵
        if (settlement.getCouponAndTemplateInfos().size() ==1){

            CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplateSDK();
            if (null == templateSDK){
                log.error("Current Not Have Coupon Can Use!!!");
                throw new CouponException("Current Not Have Coupon Can Use!!!");
            }
            //获取优惠劵类别
            CouponCategory category =CouponCategory.of(
                    templateSDK.getCategory()
            );

            switch (category){
                case LIJIAN:
                    result = executorIndex.get(RuleFlag.LIJIAN).computeRule(settlement);
                    break;
                case ZHEKOU:
                    result= executorIndex.get(RuleFlag.ZHEKOU).computeRule(settlement);
                    break;
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN).computeRule(settlement);
                    break;
            }
        }else{
            //多类优惠劵
            List<CouponCategory> categories = new ArrayList<>(settlement.getCouponAndTemplateInfos().size());

            //对优惠劵进行遍历
            settlement.getCouponAndTemplateInfos().forEach(
                    ct -> categories.add(
                            CouponCategory.of(ct.getTemplateSDK().getCategory())
                    )
            );

            //判断优惠劵的数量
            if (categories.size() != 2){
                throw new CouponException("Not Support For More Template Category");
            }else{
                // 把 满减 + 折扣 组合的优惠劵返回给优惠劵模板规则处理器接口
                if (categories.contains(CouponCategory.MANJIAN) && categories.contains(CouponCategory.ZHEKOU)){
                    result =executorIndex.get(RuleFlag.MANJIAN).computeRule(settlement);
                }else {
                    //暂不支持其他组合优惠劵
                    throw new CouponException("Not Support Other Template Category");
                }
            }
        }
        return result;
    }


    /**
     *<h2>在bean初始化之前去执行 (before)</h2>
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        //当前bean不属于 RuleExecutor 时，直接不处理
        if (! (bean instanceof RuleExecutor)){
            return bean;
        }

        //对当前bean进行强转
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();

        if (executorIndex.containsKey(ruleFlag)){
            throw new IllegalStateException("There is already an executor for Rule flag:" + ruleFlag);
        }

        log.info("Load executor {} for rule flag{}.",executor.getClass(),ruleFlag);
        executorIndex.put(ruleFlag,executor);

        return null;
    }

    /**
     *<h2>在bean初始化之后去执行(after)</h2>
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
