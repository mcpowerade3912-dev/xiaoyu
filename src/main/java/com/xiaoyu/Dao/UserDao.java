package com.xiaoyu.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu.pojo.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserDao extends BaseMapper<User> {
}
