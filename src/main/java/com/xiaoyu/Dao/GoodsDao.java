package com.xiaoyu.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu.pojo.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsDao extends BaseMapper<Goods> {
    /**
     * 乐观锁扣减库存+增加销量
     * @param goodsId 商品ID
     * @param num 扣减数量
     * @param version 当前版本号
     * @return 影响行数
     */
    @Update("UPDATE goods SET stock = stock - #{num}, sales = sales + #{num}, version = version + 1 " +
            "WHERE id = #{goodsId} AND version = #{version} AND stock >= #{num}")
    int deductStock(@Param("goodsId") Long goodsId, @Param("num") Integer num, @Param("version") Integer version);
}
