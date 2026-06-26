package com.xiaoyu.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu.pojo.UserCart;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CartDao extends BaseMapper<UserCart> {

}
