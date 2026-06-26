package com.xiaoyu.controller;

import com.xiaoyu.common.Result;
import com.xiaoyu.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {
    @Resource
    private AdminUserService adminUserService;

    // 用户分页查询
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String username) {
        return adminUserService.userPage(pageNum, pageSize, username);
    }

    // 冻结/解禁用户
    @PutMapping("/status/{userId}")
    public Result updateStatus(@PathVariable Long userId, @RequestParam Integer status) {
        return adminUserService.updateUserStatus(userId, status);
    }
}
