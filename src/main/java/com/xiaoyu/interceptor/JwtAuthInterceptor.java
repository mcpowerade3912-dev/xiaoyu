package com.xiaoyu.interceptor;

import com.xiaoyu.Dao.UserDao;
import com.xiaoyu.pojo.User;
import com.xiaoyu.utils.JwtUtil;
import com.xiaoyu.utils.UserContext;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    @Autowired
    private UserDao userDao;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        String authHeader = request.getHeader("Authorization");
        System.out.println("header完整内容：[" + authHeader + "]");
        System.out.println("是否匹配Bearer 开头：" + (authHeader != null && authHeader.startsWith("Bearer ")));
        //获得请求头中的token
        String authorization = request.getHeader("Authorization");
        //判断token是否为空
        if(authorization == null || !authorization.startsWith("Bearer ")){
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请登录\",\"data\":null}");
            return false;
        }
        String token = authorization.replace("Bearer ","");
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请登录\",\"data\":null}");
            return false;
        }

        // 解析失败返回null
        if (claims == null) {
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请登录\",\"data\":null}");
            return false;
        }

        // 1. 读取userId、role (JWT库可能返回Integer或Long，统一处理)
        Long userId;
        Integer role;
        Object userIdObj = claims.get("userId");
        Object roleObj = claims.get("role");
        if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else {
            userId = (Long) userIdObj;
        }
        if (roleObj instanceof Integer) {
            role = (Integer) roleObj;
        } else {
            role = ((Long) roleObj).intValue();
        }

        //查询用户登出状态
        User user = userDao.selectById((Long)claims.get("userId"));
        //判断用户是否存在
        if(user==null){
            response.getWriter().write("{\"code\":401,\"msg\":\"用户不存在，请重新登录\",\"data\":null}");
            return false;
        }
        if(user.getLogoutTime()!=null){
            response.getWriter().write("{\"code\":401,\"msg\":\"用户已登出，请重新登录\",\"data\":null}");
            return false;
        }

        //角色权限拦截
        //拿到当前的请求路径
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin") && role != 1) {
            response.getWriter().write("{\"code\":403,\"msg\":\"权限不足，该功能仅管理员可用\",\"data\":null}");
            return false;
        }

        //存入上下文
        UserContext.setUser(claims);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
