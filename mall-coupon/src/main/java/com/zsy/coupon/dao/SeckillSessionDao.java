package com.zsy.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsy.coupon.entity.SeckillSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 *
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
