package com.zsy.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsy.common.utils.PageUtils;
import com.zsy.ware.entity.WareInfoEntity;
import com.zsy.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 * @date 2020-08-02 15:37:46
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
    /**
     * 获取运费和收货地址信息
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

