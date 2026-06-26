package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xiaoyu.Dao.UserAddressDao;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.dto.AddressDTO;
import com.xiaoyu.pojo.UserAddress;
import com.xiaoyu.service.AddressService;
import com.xiaoyu.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class AddressServiceimpl implements AddressService {
    @Resource
    private UserContext userContext;
    @Resource
    private UserAddressDao userAddressDao;

    /*
    * 添加地址
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addAddress(AddressDTO dto) {
        Long userId = userContext.getUserId();
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);

        // 如果设置为默认，先把该用户所有地址设为非默认
        if (dto.getIsDefault() == 1) {
            LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, 0);
            userAddressDao.update(null, wrapper);
        }
        userAddressDao.insert(address);
        return new Result(Code.SUCCESS, null, "地址新增成功");

    }


    /*
    * 单条sql操作，规避并发问题
    * */
    @Override
    public Result deleteAddress(Long id) {
        Long userId = userContext.getUserId();
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getId, id)
                .eq(UserAddress::getUserId, userId);
        int rows = userAddressDao.delete(wrapper);
        if (rows == 0) {
            return new Result(Code.FAIL,null,"地址不存在或无权限");
        }
        return new Result(Code.SUCCESS,null,"删除成功");
    }

    @Override
    public Result getAddressById(Long id) {
        Long userId = userContext.getUserId();
        UserAddress address = userAddressDao.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return new Result(Code.FAIL, null, "地址不存在");
        }
        return new Result(Code.SUCCESS, address, "查询成功");

    }

    @Override
    public Result getMyAddressList() {
        Long userId = userContext.getUserId();
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId);
        // 默认地址排最前，按创建时间倒序
        wrapper.orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        List<UserAddress> list = userAddressDao.selectList(wrapper);
        return new Result(Code.SUCCESS, list, "查询成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result setDefault(Long id) {
        Long userId = userContext.getUserId();
        // 校验归属
        UserAddress address = userAddressDao.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return new Result(Code.FAIL, null, "地址不存在");
        }
        // 把该用户所有地址设为非默认
        LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0);
        userAddressDao.update(null, wrapper);
        // 把当前地址设为默认
        address.setIsDefault(1);
        userAddressDao.updateById(address);
        return new Result(Code.SUCCESS, null, "设置默认地址成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateAddress(AddressDTO dto) {
        Long userId = userContext.getUserId();
        if(dto.getId()==null){
            return new Result(Code.FAIL,null,"地址id不能为空");
        }
        // 校验地址归属
        UserAddress oldAddress = userAddressDao.selectById(dto.getId());
        if (oldAddress == null || !oldAddress.getUserId().equals(userId)) {
            return new Result(Code.FAIL, null, "地址不存在");
        }
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        BeanUtils.copyProperties(dto,address);
        // 如果设置为默认，先把该用户所有地址设为非默认
        if (dto.getIsDefault() == 1) {
            LambdaUpdateWrapper<UserAddress> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, 0);
            userAddressDao.update(null, wrapper);
        }
        userAddressDao.updateById(address);
        return new Result(Code.SUCCESS, null, "地址修改成功");
    }
}
