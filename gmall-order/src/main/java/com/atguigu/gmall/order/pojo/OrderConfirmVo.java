package com.atguigu.gmall.order.pojo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @author : panda Jian
 * @date : 2021-02-26 19:41
 * Description
 */
@Data
public class OrderConfirmVo {
    //收件人地址列表
    private List<UserAddressEntity> addresses;
    //送货清单
    private List<OrderItemVo> orderItems;
    //购买积分
    private Integer bounds;
    //为了防止重复提交，唯一标识
    private String orderToken;
}
