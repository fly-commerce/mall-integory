package com.zsy.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsy.common.to.OrderTo;
import com.zsy.common.to.mq.StockLockedTo;
import com.zsy.common.utils.PageUtils;
import com.zsy.ware.entity.WareSkuEntity;
import com.zsy.ware.vo.SkuHasStockVo;
import com.zsy.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author wanzenghui
 * @email lemon_wan@aliyun.com
 * @date 2020-08-02 15:37:46
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 添加库存
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 判断是否有库存
     */
    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 锁定库存
     */
    boolean orderLockStock(WareSkuLockVo vo);


    /**
     * 解锁库存
     */
    void unlockStock(StockLockedTo to);

    /**
     * 解锁订单
     */
    void unlockStock(OrderTo orderTo);
}

