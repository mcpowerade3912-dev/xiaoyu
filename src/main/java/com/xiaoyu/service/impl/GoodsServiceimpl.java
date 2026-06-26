package com.xiaoyu.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.Dao.GoodsDao;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.pojo.Goods;
import com.xiaoyu.dto.GoodsDto;
import com.xiaoyu.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsServiceimpl implements GoodsService {
    @Resource
    private GoodsDao goodsDao;
    /**
     * 买家端商品分页查询实现
     * @param dto
     * @return
     */
    @Override
    public Page<Goods> getGoodsPage(GoodsDto dto){
        //构建分页对象
        Page<Goods> page = new Page<>(dto.getPageNum(),dto.getPageSize());
        //构建查询条件
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        //买家端只展示上架商品
        queryWrapper.eq(Goods::getStatus,1);
        //商品名字模糊查询
        if(!StringUtils.isEmpty(dto.getGoodsName())){
            queryWrapper.like(Goods::getGoodsName,dto.getGoodsName());
        }
        //默认倒叙
        queryWrapper.orderByDesc(Goods::getCreateTime);

        //执行分页返回结果
        return goodsDao.selectPage(page,queryWrapper);
    };

    /**
     * 管理端商品分页查询实现
     * @param dto
     * @return
     */
    @Override
    public Page<Goods> adminGoodsPage(GoodsDto dto) {
        //新建分页对象
        Page<Goods> page = new Page<>(dto.getPageNum(),dto.getPageSize());
        //构建查询条件
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        if(dto.getGoodsName()!=null && !StringUtils.isEmpty(dto.getGoodsName().trim())){
            queryWrapper.like(Goods::getGoodsName,dto.getGoodsName());
        }
        queryWrapper.orderByDesc(Goods::getCreateTime);
        return goodsDao.selectPage(page,queryWrapper);
    }

    /**
     * 管理端添加商品实现
     * @param goods
     * @return
     */
    @Override
    public Result addGoods(Goods goods) {
        // 1. 查重：判断商品名称是否已存在
        LambdaQueryWrapper<Goods> repeatWrapper = new LambdaQueryWrapper<>();
        repeatWrapper.eq(Goods::getGoodsName, goods.getGoodsName().trim());
        long count = goodsDao.selectCount(repeatWrapper);
        if (count > 0) {
            return new Result(Code.FAIL, null, "该商品名称已存在，禁止重复添加");
        }
        // 新增商品默认配置：版本号1、上架状态、初始销量0
        goods.setVersion(1);
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        if (goods.getStock() == null) {
            goods.setStock(0);
        }
        if (goods.getSales() == null) {
            goods.setSales(0);
        }
        try {
            goodsDao.insert(goods);
        } catch (DuplicateKeyException e) {
            return new Result(Code.FAIL, null, "该商品名称已存在，禁止重复添加");
        }
        return new Result(Code.SUCCESS, null, "商品新增成功");
    }

    /**
     * 管理端修改商品实现
     * @param goods
     * @return
     */
    @Override
    public Result updateGoods(Goods goods) {
        // 1. 基础参数校验
        if (goods.getId() == null) {
            return new Result(Code.FAIL, null, "商品ID不能为空");
        }
        if (goods.getVersion() == null) {
            return new Result(Code.FAIL, null, "版本号不能为空，请刷新页面后重试");
        }
        String repeatMsg = "该商品名称已存在，请勿重复使用";

        // 2. 商品名称查重：排除自身ID，不能和其他商品重名
        if (goods.getGoodsName() != null && !goods.getGoodsName().trim().isEmpty()) {
            LambdaQueryWrapper<Goods> repeatWrapper = new LambdaQueryWrapper<>();
            repeatWrapper.eq(Goods::getGoodsName, goods.getGoodsName().trim())
                    .ne(Goods::getId, goods.getId()); // 排除自己
            long count = goodsDao.selectCount(repeatWrapper);
            if (count > 0) {
                return new Result(Code.FAIL, null, repeatMsg);
            }
        }

        // 禁止手动修改销量，版本号保留前端传来的值用于乐观锁校验
        goods.setSales(null);
        // 不要 goods.setVersion(null); 这行删掉！

        try {
            // 执行更新，接收影响行数
            int row = goodsDao.updateById(goods);
            if (row == 0) {
                // 乐观锁冲突：版本号不一致，数据已被其他人修改
                return new Result(Code.FAIL, null, "商品信息已被他人修改，请刷新页面重新编辑");
            }
        } catch (DuplicateKeyException e) {
            // 并发修改同名商品触发唯一索引冲突兜底
            return new Result(Code.FAIL, null, repeatMsg);
        }
        return new Result(Code.SUCCESS, null, "商品修改成功");
    }

    /**
     * 管理端修改商品状态实现
     * @param goodsId
     * @param status
     * @return
     */
    @Override
    public Result updateGoodsStatus(Long goodsId, Integer status) {
        if (goodsId == null) {
            return new Result(Code.FAIL, null, "商品ID不能为空");
        }
        if (status == null || status != 0 && status != 1) {
            return new Result(Code.FAIL, null, "状态参数非法，仅支持0下架/1上架");
        }

        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            return new Result(Code.FAIL, null, "商品不存在");
        }

        LambdaUpdateWrapper<Goods> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Goods::getId, goodsId)
                .eq(Goods::getVersion, goods.getVersion()) // 乐观锁条件
                .set(Goods::getStatus, status);

        int rows = goodsDao.update(null, wrapper);
        if (rows == 0) {
            return new Result(Code.FAIL, null, "商品信息已被其他用户修改，请刷新后重试");
        }

        String msg = status == 1 ? "商品上架成功" : "商品下架成功";
        return new Result(Code.SUCCESS, null, msg);
    }
}
