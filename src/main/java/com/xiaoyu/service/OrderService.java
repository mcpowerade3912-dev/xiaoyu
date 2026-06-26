package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.common.Result;
import com.xiaoyu.dto.OrderMainDto;
import com.xiaoyu.dto.OrderQueryDto;
import com.xiaoyu.vo.OrderDetailVO;

public interface OrderService {
    /*买家端*/
    /*创建订单*/
    Result createOrder(OrderMainDto orderMainDto);

    /*我的订单分页查询*/
    Result getMyOrderPage(OrderQueryDto queryDto);

    /*取消订单*/
    Result cancelOrder(Long orderId);

    // 确认收货（仅已发货可确认）
    Result confirmReceive(Long orderId);

    // 订单详情（联查主表+明细表）
    Result getOrderDetail(Long orderId);

    /*卖家端*/
    /*订单分页查询*/
    // 管理员全订单分页查询
    Result adminOrderPage(Integer pageNum, Integer pageSize, Integer status);
    // 订单发货
    Result deliverOrder(Long orderId);
    //订单数据统计
    Result getOrderStatistics();
}
