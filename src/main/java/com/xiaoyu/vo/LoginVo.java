package com.xiaoyu.vo;

import lombok.Data;

@Data
public class LoginVo {
    private Long userId;
    private String username;
    private String nickname;
    private Integer role;
    private String token;
}
