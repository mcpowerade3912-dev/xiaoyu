package com.xiaoyu.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVo {
    /*购物车记录的id*/
    private Long cartId;
    /*商品id*/
    private Long goodsId;
    /*商品名称*/
    private String goodsName;
    /*商品图片*/
    private String picUrl;
    /*商品价格*/
    private BigDecimal price;
    /*购买数量*/
    private Integer buyNum;
    /*商品库存*/
    private Integer stock;
    /*创建时间*/
    private LocalDateTime createTime;



}
