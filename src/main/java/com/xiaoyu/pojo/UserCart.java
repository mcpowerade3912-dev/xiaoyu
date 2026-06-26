package com.xiaoyu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@TableName("user_cart")
public class UserCart {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer buyNum;
    private LocalDateTime createTime;
}
