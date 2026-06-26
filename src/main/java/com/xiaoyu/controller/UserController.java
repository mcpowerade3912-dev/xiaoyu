package com.xiaoyu.controller;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.LoginDto;

import com.xiaoyu.dto.UserRegisterDto;
import com.xiaoyu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result UserLogin(@Validated @RequestBody LoginDto loginDto) {
        return userService.UserLogin(loginDto);
    }
    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        return userService.logout(token);
    }
    @PostMapping("/register")
    public Result register(@Validated @RequestBody UserRegisterDto userRegisterDto) {
        return userService.register(userRegisterDto);
    }
}
