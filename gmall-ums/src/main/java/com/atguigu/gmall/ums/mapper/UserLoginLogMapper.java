package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登陆记录表
 * 
 * @author panda
 * @email fengge@atguigu.com
 * @date 2021-02-21 00:29:36
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLogEntity> {
	
}
