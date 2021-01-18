package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author panda
 * @email fengge@atguigu.com
 * @date 2021-01-18 19:32:30
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponEntity> {
	
}
