package com.xiaoyu.vo;

import com.xiaoyu.pojo.Order_item;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private String addressName;
    private String addressPhone;
    private String addressDetail;
    private LocalDateTime createTime;

    // 订单商品明细列表
    private List<Order_item> itemList;
}