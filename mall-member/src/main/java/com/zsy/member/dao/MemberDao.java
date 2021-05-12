package com.zsy.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsy.member.entity.MemberEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 * @date 2020-08-02 15:18:09
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
