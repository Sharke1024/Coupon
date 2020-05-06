package com.imooc.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.imooc.coupon.constant.Constant;
import com.imooc.coupon.constant.CouponStatus;
import com.imooc.coupon.dao.CouponDao;
import com.imooc.coupon.entity.Coupon;
import com.imooc.coupon.service.IKafkaService;
import com.imooc.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 *  <h1>kafka 相关服务接口实现</h1>
 *  核心思想：
 *      将Cache 中的Coupon的状态变化同步到DB中
 * @Author DL_Wu
 * @Date 2020/5/6
 */
@Slf4j
@Component
public class KafkaServiceImpl implements IKafkaService {

    private final CouponDao couponDao;

    @Autowired
    public KafkaServiceImpl(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    /**
     * <h2>消费优惠券 Kafka 消息</h2>
     * @param record {@link ConsumerRecord}
     */
    @Override
    @KafkaListener(topics = {Constant.TOPIC}, groupId = "imooc_coupon_1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {

        //获取到 kafka消息
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        // 如果存在kafkaMessage ,装化为 CouponKafkaMessage 类型
        if (kafkaMessage.isPresent()){
            Object message = kafkaMessage.get();
            CouponKafkaMessage couponInfo = JSON.parseObject(message.toString(),CouponKafkaMessage.class);

            log.info("Receive  CouponKafkaMessage :{}",message.toString());

            CouponStatus status = CouponStatus.of(couponInfo.getStatus());

            switch (status){
                case USABLE:
                    break;
                case USED:
                    processUsedCoupons(couponInfo,status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo,status);
                    break;
            }
        }
    }

    /**
     * <h2> 处理已过期的用户优惠劵 </h2>
     * @param kafkaMessage {@link CouponKafkaMessage} 优惠劵kafka 消息对象
     * @param status {@link CouponStatus }    优惠劵状态
     */
    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage, CouponStatus status){

        // TODO 给用户发送短信
        processCouponsByStatus(kafkaMessage,status);
    }

    /**
     * <h2> 处理已使用的用户优惠劵</h2>
     * @param kafkaMessage {@link CouponKafkaMessage} 优惠劵kafka 消息对象
     * @param status {@link CouponStatus }    优惠劵状态
     */
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage, CouponStatus status){

        // TODO 给用户发送短信
        processCouponsByStatus(kafkaMessage,status);
    }

    /**
     * <h2>根据状态处理优惠劵信息</h2>
     * @param kafkaMessage {@link CouponKafkaMessage} 优惠劵kafka 消息对象
     * @param status {@link CouponStatus }    优惠劵状态
     */
    private void processCouponsByStatus(CouponKafkaMessage kafkaMessage , CouponStatus status){
        List<Coupon> coupons = couponDao.findAllById(kafkaMessage.getIds());

        if (CollectionUtils.isEmpty(coupons) || coupons.size() != kafkaMessage.getIds().size()){
            log.error("Can NOt Find Right Coupon Info :{}",JSON.toJSONString(kafkaMessage));
            // TODO  发送邮件或短信
            return;
        }

        coupons.forEach(c -> c.setStatus(status));

        log.info("CouponKafkaMessage Op Coupon Count :{}",couponDao.saveAll(coupons).size());
    }
}
