package com.xiaoyu.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu.pojo.UserAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAddressDao extends BaseMapper<UserAddress> {
}