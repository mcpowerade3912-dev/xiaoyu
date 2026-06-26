package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.common.Result;
import com.xiaoyu.pojo.Goods;
import com.xiaoyu.dto.GoodsDto;

/*买家端商品分页查询*/
public interface GoodsService {
    Page<Goods> getGoodsPage(GoodsDto dto);

    // 管理员分页查询商品（含下架商品）
    Page<Goods> adminGoodsPage(GoodsDto dto);

    // 新增商品
    Result addGoods(Goods goods);

    // 编辑商品
    Result updateGoods(Goods goods);

    // 商品上下架
    Result updateGoodsStatus(Long goodsId, Integer status);

}
