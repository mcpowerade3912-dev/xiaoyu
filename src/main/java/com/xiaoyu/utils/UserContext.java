package com.xiaoyu.utils;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
//存储当前登录用户上下文
@Component
public class UserContext {
    //线程本地存储容器：每个请求线程独有一份数据，线程之间互不干扰、数据隔离。
    private static ThreadLocal<Claims> threadLocal = new ThreadLocal<>();
    public static void setUser(Claims claims) {
        //把解析出来的用户信息存入当前请求的专属线程
        threadLocal.set(claims);
    }

    /*
    threadLocal.get()：取出当前线程里存的 Claims（用户信息）；
    .getSubject()：取出 JWT 里存的用户数据
    Long.valueOf(...) 转成 Long 类型 ID*/
    public Long getUserId(){
        return Long.parseLong(threadLocal.get().get("userId").toString());
    }
    public Integer getUserRole(){
        return Integer.parseInt(threadLocal.get().get("role").toString());
    }
    /*释放，防止内存泄漏*/
    public static void clear() {
        threadLocal.remove();
    }
}
