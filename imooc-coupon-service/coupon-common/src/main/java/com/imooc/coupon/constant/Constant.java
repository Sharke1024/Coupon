package com.imooc.coupon.constant;

/**
 *  <h1>定义常量定义</h1>
 * @Author DL_Wu
 * @Date 2020/4/29
 */
public class Constant {

    /** Kafka 消息的 Topic */
    public static  final String TOPIC = "imooc_user_coupon_op";

    /**
     * <h2>Redis Key 前缀定义</h2>
     */
    public class redisPrefix{
        /** 优惠劵 key 前缀*/
        public static final String COUPON_TEMPLATE =
                "imooc_coupon_template_code_";

        /** 用户当前所有可用的优惠券 key 前缀 */
        public static final String USER_COUPON_USABLE=
                "imooc_user_coupon_usable";

        /** 用户当前所有已使用的优惠券 key 前缀 */
        public static final String USER_COUPON_USED=
                "imooc_user_coupon_used";

        /** 用户当前所有已过期的优惠券 key 前缀 */
        public static final String USER_COUPON_EXPIRED =
                "imooc_user_coupon_expired";

    }

}
