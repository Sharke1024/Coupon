package com.imooc.coupon.schedule;

import com.imooc.coupon.dao.CouponTemplateDao;
import com.imooc.coupon.entity.CouponTemplate;
import com.imooc.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *<h1>定时清理已过期的优惠券模板</h1>
 * @Author DL_Wu
 * @Date 2020/4/30
 */
@Component
@Slf4j
public class ScheduleTask {

    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduleTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    /**
     *  <h2>下线已过期的优惠券模板</h2>
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000)  //每小时清理一次
    public void offlineCouponTemplate() {

        log.info("Start To Expire CouponTemplate");

        //查找所有未过期的模板
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);

        if (CollectionUtils.isEmpty(templates)) {
            log.info("Done To Expire CouponTemplate.");
            return;
        }

        //设置当前时间
        Date cur = new Date();

        ArrayList<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());

        templates.forEach(t -> {
            // 根据优惠券模板规则中的 "过期规则" 校验模板是否过期
            TemplateRule rule =t.getRule();
            if (rule.getExpiration().getDeadline() < cur.getTime()){
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });

        if (CollectionUtils.isNotEmpty(expiredTemplates)){
            log.info("Expired CouponTemplate Num: {}",
                    templateDao.saveAll(expiredTemplates));
        }

        log.info("Done To Expire CouponTemplate");
    }

}
