package com.zsy.order.vo;

import com.zsy.order.entity.OrderEntity;
import lombok.Data;

/**
 * 提交订单返回结果
 * @author: wan
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /** 错误状态码 0成功**/
    private Integer code;


}
