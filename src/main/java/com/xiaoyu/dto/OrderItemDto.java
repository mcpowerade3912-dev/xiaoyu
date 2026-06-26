package com.xiaoyu.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/*商品项子Dto*/
@Data
public class OrderItemDto {
    @NotNull(message="商品id不能为空")
    private Long goodsId;

    @NotNull(message="购买数量不能为空")
    @Min(value=1,message="购买数量不能小于1")
    private Integer buyNum;
}
