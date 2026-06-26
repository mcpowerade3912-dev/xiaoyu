package com.xiaoyu.controller;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.CartDto;
import com.xiaoyu.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @PostMapping("/add")
    public Result addCart(@Validated @RequestBody CartDto cartDto){
        return cartService.addCart(cartDto);
    }

    @GetMapping("/get")
    public Result getCart(){
        return cartService.getListCart(null);
    }
    @PutMapping("/update")
    public Result updateCart(@Validated @RequestBody CartDto cartDto){
        return cartService.updateCartNum(cartDto);
    }
}
