package com.xiaoyu.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("goods")
public class Goods {
    private Long id;

    @TableField("goods_name")
    private String goodsName;

    private BigDecimal price;
    private Integer stock ;
    private String pic_url;
    private Integer sales;

    @Version
    private Integer version;

    private Integer status;
    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String update_time;
}
