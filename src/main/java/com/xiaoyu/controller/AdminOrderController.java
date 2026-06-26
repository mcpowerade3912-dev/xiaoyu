package com.xiaoyu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.common.Result;
import com.xiaoyu.service.OrderService;
import com.xiaoyu.vo.OrderStatisticsVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {
    @Resource
    private OrderService orderService;

    // 全平台订单分页查询
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer status) {
        return orderService.adminOrderPage(pageNum, pageSize, status);
    }

    // 订单发货
    @PutMapping("/deliver/{orderId}")
    public Result deliver(@PathVariable Long orderId) {
        return orderService.deliverOrder(orderId);
    }

    // 订单数据统计
    @GetMapping("/statistics")
    public Result statistics() {
        return orderService.getOrderStatistics();
    }
}
