package com.atguigu.gmall.sms.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-01-20 20:14
 * Description
 */
@Data
public class SkuSaleVo {
    private Long skuId;
    //积分优惠信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;
    //满减优惠信息
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
    //打折优惠信息
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;
}
