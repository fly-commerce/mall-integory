package com.zsy.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsy.common.utils.PageUtils;
import com.zsy.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 * @date 2020-08-02 15:37:46
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

