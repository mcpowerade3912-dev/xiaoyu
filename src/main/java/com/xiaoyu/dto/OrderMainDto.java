package com.xiaoyu.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/*下单主请求Dto*/
@Data
public class OrderMainDto {
    @NotNull(message = "收件人姓名不能为空")
    private String addressName;

    @NotNull(message = "收件人手机号不能为空")
    private String addressPhone;

    @NotNull(message = "收件人地址不能为空")
    private String addressDetail;

    @NotNull(message = "商品列表不能为空")
    @Valid
    private List<OrderItemDto> goodsList;
}
