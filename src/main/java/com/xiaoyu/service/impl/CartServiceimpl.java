package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoyu.Dao.CartDao;
import com.xiaoyu.Dao.GoodsDao;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.dto.CartDto;
import com.xiaoyu.pojo.Goods;
import com.xiaoyu.pojo.UserCart;
import com.xiaoyu.service.CartService;
import com.xiaoyu.utils.UserContext;
import com.xiaoyu.vo.CartVo;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;

import java.util.List;


@Service
public class CartServiceimpl implements CartService {
    @Resource
    private CartDao cartDao;
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private UserContext userContext;

    /*添加商品大到购物车*/
    public Result addCart(CartDto cartDto){
        //获取当前用户ID
        Long userId = userContext.getUserId();
        //获取商品信息
        Goods goods = goodsDao.selectById(cartDto.getGoodsId());

//        System.out.println("===查询条件===");
//        System.out.println("登录用户ID：" + userId);
//        System.out.println("前端传的商品ID：" + cartDto.getGoodsId());

        //判断商品是否存在
        if(goods == null){
            return new Result("500","商品不存在");
        }
        //判断商品是否下架
        if(goods.getStatus() != 1){
            return new Result("500","商品已下架");
        }
        LambdaQueryWrapper<UserCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCart::getGoodsId,cartDto.getGoodsId()).eq(UserCart::getUserId,userId);
        UserCart exitcart = cartDao.selectOne(queryWrapper);

//        System.out.println("查询到购物车：" + exitcart);
//        System.out.println("buy_num值：" + exitcart.getBuyNum());

        //判断商品是否已存在购物车
        //如果存在
        if(exitcart != null){
            //累加数量
            int totalMun = exitcart.getBuyNum() + cartDto.getBuyNum();
            //判断库存
            if(totalMun > goods.getStock()){
                return new Result("500","商品库存不足");
            }
            exitcart.setBuyNum(totalMun);
            cartDao.updateById(exitcart);

            CartVo cartVo = new CartVo();
            cartVo.setCartId(exitcart.getId());
            cartVo.setGoodsId(exitcart.getGoodsId());
            cartVo.setGoodsName(goods.getGoodsName());
            cartVo.setPicUrl(goods.getPic_url());
            cartVo.setPrice(goods.getPrice());
            cartVo.setBuyNum(exitcart.getBuyNum());
            cartVo.setStock(goods.getStock());
            cartVo.setCreateTime(LocalDateTime.now());
            return new Result(Code.SUCCESS,cartVo, "加入购物车成功");
        }else{
            if(cartDto.getBuyNum() > goods.getStock()){
                return new Result("500","商品库存不足");
            }
            UserCart cart = new UserCart();
            cart.setUserId(userId);
            cart.setGoodsId(cartDto.getGoodsId());
            cart.setBuyNum(cartDto.getBuyNum());
            cartDao.insert(cart);

            CartVo cartVo = new CartVo();
            cartVo.setCartId(cart.getId());
            cartVo.setGoodsId(cart.getGoodsId());
            cartVo.setGoodsName(goods.getGoodsName());
            cartVo.setPicUrl(goods.getPic_url());
            cartVo.setPrice(goods.getPrice());
            cartVo.setBuyNum(cart.getBuyNum());
            cartVo.setStock(goods.getStock());
            cartVo.setCreateTime(LocalDateTime.now());

            return new Result(Code.SUCCESS,cartVo, "加入购物车成功");
        }
    }

    /*查询购物车*/
    public Result getListCart(CartDto cart){
        Long userId = userContext.getUserId();
        LambdaQueryWrapper<UserCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCart::getUserId,userId);
        List<UserCart> carts = cartDao.selectList(queryWrapper);
        return new Result(Code.SUCCESS,carts, "查询购物车成功");
    }

    /*修改购物车数量*/
    public Result updateCartNum(CartDto dto){
        Long userId = userContext.getUserId();

        LambdaQueryWrapper<UserCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCart::getGoodsId,dto.getGoodsId()).eq(UserCart::getUserId,userId);
        UserCart cart = cartDao.selectOne(queryWrapper);
        if( cart == null){
            return new Result("500","购物车记录不存在");
        }
        //判断商品状态
        Goods goods = goodsDao.selectById(dto.getGoodsId());
        if(goods == null){
            return new Result("500","商品不存在");
        }
        if(goods.getStatus() != 1){
            return new Result("500","商品已下架");
        }
        if(dto.getBuyNum() > goods.getStock()){
            return new Result("500","商品库存不足");
        }
        cart.setBuyNum(dto.getBuyNum());
        cartDao.updateById(cart);
        CartVo cartVo = new CartVo();
        cartVo.setCartId(cart.getId());
        cartVo.setGoodsId(cart.getGoodsId());
        cartVo.setGoodsName(goods.getGoodsName());
        cartVo.setPicUrl(goods.getPic_url());
        cartVo.setPrice(goods.getPrice());
        cartVo.setStock(goods.getStock());
        cartVo.setBuyNum(cart.getBuyNum());
        cartVo.setCreateTime(LocalDateTime.now());
        return new Result(Code.SUCCESS,cartVo, "修改购物车数量成功");
    }

    /*删除购物车内订单*/
    public  Result deleteCart(CartDto cartDto){
        Long userId = userContext.getUserId();
        LambdaQueryWrapper<UserCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCart::getGoodsId,cartDto.getGoodsId()).eq(UserCart::getUserId,userId);
        int count = cartDao.delete(queryWrapper);
        if( count == 0){
            return new Result("500","购物车记录不存在");
        }else{
            return new Result(Code.SUCCESS,"删除购物车成功");
        }
    }
}
