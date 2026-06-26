package com.xiaoyu.service;

import com.xiaoyu.common.Result;

public interface AdminUserService {
    // 用户分页查询，支持用户名模糊搜索
    Result userPage(Integer pageNum, Integer pageSize, String username);

    // 冻结/解禁用户
    Result updateUserStatus(Long userId, Integer status);
}
