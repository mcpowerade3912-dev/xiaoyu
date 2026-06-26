package com.xiaoyu.service;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.AddressDTO;

public interface AddressService {
    // 新增收货地址
    Result addAddress(AddressDTO dto);

    // 编辑收货地址
    Result updateAddress(AddressDTO dto);

    // 删除收货地址
    Result deleteAddress(Long id);

    // 设置默认地址
    Result setDefault(Long id);

    // 查询我的地址列表
    Result getMyAddressList();

    // 查询地址详情
    Result getAddressById(Long id);
}
