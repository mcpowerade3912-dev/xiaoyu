package com.xiaoyu.service;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.LoginDto;
import com.xiaoyu.dto.UserRegisterDto;

public interface UserService {
    /*
    * 用户登录服务
    * */
    Result UserLogin(LoginDto loginDto);

    /*
    * 用户登出服务*/
    Result logout(String token);

    /*
    * 用户注册服务
    * */
    Result register(UserRegisterDto userRegisterDto);
}
