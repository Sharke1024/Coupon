package com.imooc.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;


/**
 * <h1>Kafka 相关服务接口定义</h1>
 *
 * @Author DL_Wu
 * @Date 2020/5/4
 */
public interface IKafkaService {

    /**
     * <h2>消费优惠券 Kafka 消息</h2>
     * @param record {@link ConsumerRecord}
     */
    void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record);

}
