package com.xiaoyu.controller;

import com.xiaoyu.common.Result;
import com.xiaoyu.dto.GoodsDto;
import com.xiaoyu.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @GetMapping("/page")
    public Result getGoodsPage(@Validated GoodsDto dto){
        return new Result("200",goodsService.getGoodsPage(dto),"查询成功");
    }

}
