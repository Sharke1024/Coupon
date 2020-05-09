package com.imooc.coupon.executor;

import com.imooc.coupon.constant.RuleFlag;
import com.imooc.coupon.vo.SettlementInfo;

/**
 *  <h1>优惠劵模板规则处理器接口定义</h1>
 * @Author DL_Wu
 * @Date 2020/5/9
 */
public interface RuleExecutor {

    /**
     * <h1>优惠规则类型标记</h1>
     * @return {@link RuleFlag}
     * */
    RuleFlag ruleConfig();

    /**
     * <h1>优惠规则的计算</h1>
     * @param settlement{@link SettlementInfo} 包含了选择的优惠劵
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    SettlementInfo computeRule(SettlementInfo settlement);

}
