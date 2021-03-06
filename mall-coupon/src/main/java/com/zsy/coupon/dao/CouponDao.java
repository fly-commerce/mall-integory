package com.zsy.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsy.coupon.entity.CouponEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 *
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
