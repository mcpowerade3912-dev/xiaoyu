package com.xiaoyu.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/*
全局订单号生成工具
*/
@Component
public class OrderNoutil {
    //时间格式化器，将时间格式化成17位
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    //随机数生成器
    public static final Random RANDOM = new Random();

    /*
    * 全局订单号生成规则:17位时间戳+用户id后六位+4位随机数
    * @Param userId
    * @return 订单号
    * */
    public static String getOrderNo(Long userId){
        String timeStr = FORMATTER.format(LocalDateTime.now());
        String randomStr = String.format("%04d", RANDOM.nextInt(10000));
        String userIdStr = String.format("%06d", userId%1000000);
        return timeStr+userIdStr+randomStr;
    }
}
