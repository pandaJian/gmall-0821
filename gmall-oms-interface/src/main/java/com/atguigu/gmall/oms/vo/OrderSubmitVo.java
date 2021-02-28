package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-02-27 22:53
 * Description
 */
@Data
public class OrderSubmitVo {
    private String orderToken; //防重
    private UserAddressEntity address; //收货地址
    private Integer payType;
    private String deliveryCompany;
    private Integer bounds;
    private List<OrderItemVo> items;
    private BigDecimal totalPrice; //验总价所需的字段
}
