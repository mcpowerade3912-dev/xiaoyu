package com.xiaoyu.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserRegisterDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2,max = 30,message = "用户名长度在2-30之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 16,message = "密码长度在6-16之间")
    private String password;

    private String nickname;
    private String phone;

}
