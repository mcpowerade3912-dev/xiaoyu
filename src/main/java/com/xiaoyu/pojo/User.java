package com.xiaoyu.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    public String username;

//    @TableField(exist = false)//不映射数据库字段
    public String password;

    public String nickname;
    public String phone;
    private Integer role;
    public Integer status;
    public Date createTime;

    // 新增登出时间标记
    @TableField(value="logout_time",updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime logoutTime;
}
