package com.imooc.coupon.vo;

import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.constant.PeriodType;
import com.imooc.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1>优惠劵状态分发</h1>
 *
 * @Author DL_Wu
 * @Date 2020/5/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {

    /** 可使用的 */
    private List<Coupon> usable;

    /** 已使用的 */
    private List<Coupon> used;

    /** 已过期的 **/
    private List<Coupon> expired;

    public static CouponClassify classify(List<Coupon> coupons){
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach( c-> {
            // 判断优惠劵是否过期
            boolean isTimeExpired;
            long curTime = new Date().getTime();
            //判断优惠劵的类型是否是固定的
            if (c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(PeriodType.REGULAR)){
                //固定日期
                isTimeExpired = c.getTemplateSDK().getRule().getExpiration().getDeadline() <= curTime;
            }else{
                //非固定日期，自领取之日计算
                isTimeExpired = DateUtils.addDays(c.getAssignTime(),
                        c.getTemplateSDK().getRule().getExpiration().getGap()).getTime() <= curTime;
            }

            //将优惠劵分别加入到不同 状态中
            if (c.getStatus() == CouponStatus.USED){
                used.add(c);
            }else if (c.getStatus() == CouponStatus.EXPIRED || isTimeExpired){
                expired.add(c);
            }else{
                usable.add(c);
            }
        });

        return new CouponClassify(usable,used,expired);
    }

}
