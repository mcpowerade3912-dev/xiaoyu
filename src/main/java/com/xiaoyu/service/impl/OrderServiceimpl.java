package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.Dao.GoodsDao;
import com.xiaoyu.Dao.OrderItemDao;
import com.xiaoyu.Dao.OrderMainDao;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.dto.OrderItemDto;
import com.xiaoyu.dto.OrderMainDto;
import com.xiaoyu.dto.OrderQueryDto;
import com.xiaoyu.pojo.Goods;
import com.xiaoyu.pojo.Order_item;
import com.xiaoyu.pojo.Order_main;
import com.xiaoyu.service.OrderService;
import com.xiaoyu.utils.UserContext;
import com.xiaoyu.vo.OrderDetailVO;
import com.xiaoyu.vo.OrderStatisticsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.xiaoyu.utils.OrderNoutil.getOrderNo;

@Service
public class OrderServiceimpl implements OrderService {
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private OrderMainDao orderMainDao;
    @Resource
    private OrderItemDao orderItemDao;
    @Resource
    private UserContext userContext;
    /*
    *用户结算下单
    * @param orderCreateDTO 下单参数
    * @return 生成的订单号
    * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createOrder(OrderMainDto orderMainDto){
        //从上下文获取用户id
        Long userId = userContext.getUserId();

        //生成全局订单号
        String orderNo = getOrderNo(userId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Order_item> orderItemList = new ArrayList<>();

        //遍历商品：校验+乐观锁减库存+构建订单明细
        for(OrderItemDto orderItemDto : orderMainDto.getGoodsList()){
            Goods goods = goodsDao.selectById(orderItemDto.getGoodsId());

            if(goods == null){
                return new Result(Code.FAIL,null,"商品id"+orderItemDto.getGoodsId()+"不存在");
            }
            if(goods.getStatus() != 1){
                return new Result(Code.FAIL,null,"商品"+goods.getGoodsName()+"已下架");
            }
            if(orderItemDto.getBuyNum() > goods.getStock()){
                return new Result(Code.FAIL,null,"商品"+goods.getGoodsName()+"库存不足");
            }
            int row = goodsDao.deductStock(goods.getId(),orderItemDto.getBuyNum(),goods.getVersion());
            if(row == 0){
                return new Result(Code.FAIL,null,"下单失败，请刷新");
            }

            //累加订单总金额=商品单价*商品数量
            BigDecimal itemAmount = goods.getPrice().multiply(BigDecimal.valueOf(orderItemDto.getBuyNum()));
            totalAmount = totalAmount.add(itemAmount);

            // 构建订单明细（订单快照：存下单时的商品名和价格，后续商品修改不影响历史订单）
            Order_item item = new Order_item();
            item.setOrderNo(orderNo);
            item.setGoodsId(goods.getId());
            item.setUserId(userId);
            item.setGoodsName(goods.getGoodsName());
            item.setBuyNum(orderItemDto.getBuyNum());
            item.setGoodsPrice(goods.getPrice());
            orderItemDao.insert(item);
            orderItemList.add(item);
        }
        // 5. 插入订单主表
        Order_main orderMain = new Order_main();
        orderMain.setOrderNo(orderNo);
        orderMain.setUserId(userId);
        orderMain.setTotalAmount(totalAmount);
        orderMain.setPayAmount(totalAmount); // 暂无优惠，实付=总金额
        orderMain.setStatus(0); // 0=待付款
        orderMain.setAddressName(orderMainDto.getAddressName());
        orderMain.setAddressPhone(orderMainDto.getAddressPhone());
        orderMain.setAddressDetail(orderMainDto.getAddressDetail());
        orderMainDao.insert(orderMain);

        return new Result(Code.SUCCESS,userId,"下单成功");
    }

    @Override
    public Result getMyOrderPage(OrderQueryDto queryDto){
        Long userId = userContext.getUserId();
        //构建分页对象
        Page<Order_main> page = new Page<>(queryDto.getPageNum(),queryDto.getPageSize());
        LambdaQueryWrapper<Order_main> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order_main::getUserId,userId);
        wrapper.orderByDesc(Order_main::getCreateTime);
        // 按状态筛选
        if (queryDto.getStatus() != null) {
            wrapper.eq(Order_main::getStatus, queryDto.getStatus());
        }

        return new Result(Code.SUCCESS,orderMainDao.selectPage(page,wrapper),"查询成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public  Result cancelOrder(Long orderId){
        Long userId = userContext.getUserId();
        //查询订单，校验归属
        Order_main order = orderMainDao.selectById(orderId);
        if(order == null||!order.getUserId().equals(userId)){
            return new Result(Code.FAIL,null,"订单不存在");
        }
        // 2. 校验订单状态：仅待付款（0）可取消
        if (order.getStatus() != 0) {
            return new Result(Code.FAIL, null, "仅待付款订单可取消");
        }
        //乐观锁更新订单状态为“已取消”
        order.setStatus(4);
        LambdaQueryWrapper<Order_item> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(Order_item::getOrderNo, order.getOrderNo());
        List<Order_item> itemList = orderItemDao.selectList(itemWrapper);

        for (Order_item item : itemList) {
            Goods goods = goodsDao.selectById(item.getGoodsId());
            if (goods != null) {
                // 归还库存+回退销量，乐观锁自动处理版本号
                goods.setStock(goods.getStock() + item.getBuyNum());
                goods.setSales(goods.getSales() - item.getBuyNum());
                int stockRows = goodsDao.updateById(goods);
                if (stockRows == 0) {
                    throw new RuntimeException("库存归还失败，订单取消回滚");
                }
            }
        }

        return new Result(Code.SUCCESS, null, "订单取消成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result confirmReceive(Long orderId){
        Long userId = userContext.getUserId();
        // 1. 校验订单归属
        Order_main order = orderMainDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return new Result(Code.FAIL, null, "订单不存在");
        }
        // 2. 校验状态：仅已发货（2）可确认收货
        if (order.getStatus() != 2) {
            return new Result(Code.FAIL, null, "仅已发货订单可确认收货");
        }
        // 3. 乐观锁更新状态为已完成（3），设置完成时间
        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        int rows = orderMainDao.updateById(order);
        if (rows == 0) {
            return new Result(Code.FAIL, null, "订单状态已变更，请刷新后重试");
        }
        return new Result(Code.SUCCESS, null, "确认收货成功");
    }

    @Override
    public Result getOrderDetail(Long orderId){
        Long userId = userContext.getUserId();
        // 1. 校验订单归属
        Order_main order = orderMainDao.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return new Result(Code.FAIL, null, "订单不存在");
        }
        // 2. 查询订单明细列表
        LambdaQueryWrapper<Order_item> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(Order_item::getOrderNo, order.getOrderNo());
        List<Order_item> itemList = orderItemDao.selectList(itemWrapper);
        // 3. 组装返回VO
        OrderDetailVO detailVO = new OrderDetailVO();
        BeanUtils.copyProperties(order, detailVO);
        detailVO.setItemList(itemList);
        return new Result(Code.SUCCESS, detailVO, "查询成功");

    }

    /*管理员，发货*/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deliverOrder(Long orderId) {
        Order_main order = orderMainDao.selectById(orderId);
        if (order == null) {
            return new Result(Code.FAIL, null, "订单不存在");
        }
        if (order.getStatus() != 1) {
            return new Result(Code.FAIL, null, "仅待发货订单可发货");
        }
        // 3. 乐观锁更新状态为已发货（2），自动携带version条件，防并发覆盖
        order.setStatus(2);
        order.setDeliveryTime(LocalDateTime.now());
        int rows = orderMainDao.updateById(order);
        if (rows == 0) {
            return new Result(Code.FAIL, null, "订单状态已变更，请刷新后重试");
        }
        return new Result(Code.SUCCESS, null, "发货成功");
    }
    /*管理员订单分页查询*/
    @Override
    public Result adminOrderPage(Integer pageNum, Integer pageSize, Integer status) {
        Page<Order_main> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order_main> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order_main::getStatus, status);
        }
        wrapper.orderByDesc(Order_main::getCreateTime);
        Page<Order_main> result = orderMainDao.selectPage(page, wrapper);
        return new Result(Code.SUCCESS, result, "查询成功");
    }
    /*管理员查看订单数据*/
    @Override
    public Result getOrderStatistics(){
        OrderStatisticsVO statistics = orderMainDao.getOrderStatistics();
        return new Result(Code.SUCCESS, statistics, "查询成功");
    }
}
