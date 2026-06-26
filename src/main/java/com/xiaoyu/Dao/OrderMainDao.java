package com.xiaoyu.Dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu.pojo.Order_main;
import com.xiaoyu.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMainDao extends BaseMapper<Order_main> {

    /**
     * 订单数据统计
     * status>=1 表示已付款及以上订单计入成交数据
     */
    @Select("SELECT " +
            "COUNT(*) AS totalOrderCount, " +
            "IFNULL(SUM(pay_amount), 0) AS totalAmount, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) AS todayOrderCount, " +
            "IFNULL(SUM(CASE WHEN DATE(create_time) = CURDATE() THEN pay_amount ELSE 0 END), 0) AS todayTotalAmount " +
            "FROM order_main " +
            "WHERE status >= 1")
    OrderStatisticsVO getOrderStatistics();
}
