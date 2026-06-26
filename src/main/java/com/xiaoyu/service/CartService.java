package com.xiaoyu.service;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.CartDto;

import java.util.List;

public interface CartService {
    /*添加商品大到购物车*/
    Result addCart(CartDto cartDto);
    /*查询购物车*/
    Result getListCart(CartDto cartDto);
    /*修改购物车数量*/
    Result updateCartNum(CartDto cartDto);
    /*删除购物车*/
    Result deleteCart(CartDto cartDto);
}
