package com.xiaoyu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.common.Result;
import com.xiaoyu.dto.GoodsDto;
import com.xiaoyu.pojo.Goods;
import com.xiaoyu.service.GoodsService;
import com.xiaoyu.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;
    // 管理员分页查询商品
    @PostMapping("/page")
    public Page<Goods> page(@Validated @RequestBody GoodsDto dto) {
        return goodsService.adminGoodsPage(dto);
    }

    // 新增商品
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        return goodsService.addGoods(goods);
    }

    // 编辑商品
    @PutMapping("/update")
    public Result update(@RequestBody Goods goods) {
        return goodsService.updateGoods(goods);
    }

    // 商品上下架
    @PutMapping("/status/{goodsId}")
    public Result updateStatus(@PathVariable Long goodsId, @RequestParam Integer status) {
        return goodsService.updateGoodsStatus(goodsId, status);
    }
}
