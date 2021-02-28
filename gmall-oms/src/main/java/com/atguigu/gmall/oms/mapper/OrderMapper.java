package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author panda
 * @email fengge@atguigu.com
 * @date 2021-02-28 11:46:35
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
