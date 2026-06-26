package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.Dao.UserDao;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.pojo.User;
import com.xiaoyu.service.AdminUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AdminUserServiceimpl implements AdminUserService {
    @Resource
    private UserDao userDao;

    @Override
    public Result userPage(Integer pageNum, Integer pageSize, String username) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userDao.selectPage(page, wrapper);
        return new Result(Code.SUCCESS, result, "查询成功");
    }

    @Override
    public Result updateUserStatus(Long userId, Integer status) {
        User user = userDao.selectById(userId);
        if (user == null) {
            return new Result(Code.FAIL, null, "用户不存在");
        }
        user.setStatus(status);
        userDao.updateById(user);
        String msg = status == 1 ? "用户解禁成功" : "用户冻结成功";
        return new Result(Code.SUCCESS, null, msg);
    }
}
