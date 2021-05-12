package com.zsy.order.feign;


import com.zsy.common.utils.R;
import com.zsy.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeignService {

    /**
     * 查询sku是否有库存
     */
    @PostMapping(value = "/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    /**
     * 查询运费和收货地址信息
     */
    @GetMapping(value = "/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 锁定库存
     */
    @PostMapping(value = "/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
