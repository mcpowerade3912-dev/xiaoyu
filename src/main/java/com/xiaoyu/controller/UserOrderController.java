package com.xiaoyu.controller;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.OrderMainDto;
import com.xiaoyu.dto.OrderQueryDto;
import com.xiaoyu.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/order")
public class UserOrderController {

    @Resource
    private OrderService orderService;

    /**
     * 用户结算下单
     */
    @PostMapping("/create")
    public Result createOrder(@Validated @RequestBody OrderMainDto orderMainDto) {
        return orderService.createOrder(orderMainDto);
    }

    // 我的订单分页查询
    @GetMapping("/page")
    public Result getMyOrderPage(@Validated OrderQueryDto queryDto) {
        return orderService.getMyOrderPage(queryDto);
    }

    // 取消订单
    @PutMapping("/cancel/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    // 确认收货
    @PutMapping("/confirm/{orderId}")
    public Result confirmReceive(@PathVariable Long orderId) {
        return orderService.confirmReceive(orderId);
    }

    // 订单详情
    @GetMapping("/detail/{orderId}")
    public Result getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(orderId);
    }


}
