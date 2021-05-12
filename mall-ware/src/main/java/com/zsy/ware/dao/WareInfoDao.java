package com.zsy.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsy.ware.entity.WareInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 * @date 2020-08-02 15:37:46
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
