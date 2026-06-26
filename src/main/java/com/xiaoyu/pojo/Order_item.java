package com.xiaoyu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@TableName("order_item")
@Data
public class Order_item {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String orderNo;
    private Long goodsId;
    private String goodsName;
    private BigDecimal goodsPrice;
    private Integer buyNum;
    private String createTime;

}
