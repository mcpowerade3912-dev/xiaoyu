package com.xiaoyu.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderQueryDto {
    @NotNull(message = "页码不能为空")
    private Integer pageNum = 1;

    @NotNull(message = "每页条数不能为空")
    private Integer pageSize = 10;

    // 订单状态筛选：0待付款 1已付款 2已发货 3已完成 4已取消 5已退款
    private Integer status;
}
