package com.imooc.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h1>商品类型枚举</h1>
 * @Author DL_Wu
 * @Date 2020/5/4
 */
@Getter
@AllArgsConstructor
public enum GoodsType {

    WENYU("文娱",1),
    SHANGXIAN("生鲜",2),
    JIAJU("家具",3),
    OTHERS("其他",4),
    ALL("全品类",5);

    //商品类类型描述
    private String description;

    //商品类型编码
    private Integer code;

    public static GoodsType of(Integer code){
        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean-> bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code + " Is Not Exist! "));
    }


}
