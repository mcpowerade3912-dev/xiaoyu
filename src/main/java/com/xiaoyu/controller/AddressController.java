package com.xiaoyu.controller;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.AddressDTO;
import com.xiaoyu.service.AddressService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Resource
    private AddressService addressService;
    // 添加地址
    @PostMapping("/add")
    public Result addAddress(@Validated @RequestBody AddressDTO dto) {
        return addressService.addAddress(dto);
    }
    // 编辑地址
    @PutMapping("/update")
    public Result updateAddress(@Validated @RequestBody AddressDTO dto) {
        return addressService.updateAddress(dto);
    }

    // 删除地址
    @DeleteMapping("/{id}")
    public Result deleteAddress(@PathVariable Long id) {
        return addressService.deleteAddress(id);
    }

    // 设置默认地址
    @PutMapping("/default/{id}")
    public Result setDefault(@PathVariable Long id) {
        return addressService.setDefault(id);
    }

    // 我的地址列表
    @GetMapping("/list")
    public Result getMyAddressList() {
        return addressService.getMyAddressList();
    }

    // 地址详情
    @GetMapping("/{id}")
    public Result getAddressById(@PathVariable Long id) {
        return addressService.getAddressById(id);
    }
}
