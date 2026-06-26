package com.xiaoyu.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class GoodsDto {
    //页码
    @NotNull(message = "页码不能为空")
    private Integer pageNum;
    //每页数量
    @NotNull(message = "每页数量不能为空")
    private Integer pageSize;
    //商品名称
    private String goodsName;
}
