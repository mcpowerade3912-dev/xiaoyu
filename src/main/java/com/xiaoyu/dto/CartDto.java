package com.xiaoyu.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CartDto {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    private Integer buyNum;

}
