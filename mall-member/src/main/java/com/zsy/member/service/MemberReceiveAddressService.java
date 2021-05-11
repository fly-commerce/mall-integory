package com.zsy.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsy.common.utils.PageUtils;
import com.zsy.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author zsy
 * @email 594983498@qq.com
 * @date 2019-10-08 09:47:05
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @Description TODO: 根据会员id查询地址
     * @Author fly-ftx
     * @Date 21:04 2021/5/11
     * @Param [memberId]
     * @return java.util.List<com.zsy.member.entity.MemberReceiveAddressEntity>
     **/
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

