package com.zsy.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsy.common.utils.PageUtils;
import com.zsy.ware.entity.PurchaseEntity;
import com.zsy.ware.vo.MergeVo;
import com.zsy.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 * @date 2020-08-02 15:37:46
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo purchaseDoneVo);
}

