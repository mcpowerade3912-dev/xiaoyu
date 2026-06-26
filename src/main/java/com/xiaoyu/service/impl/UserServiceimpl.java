package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoyu.common.Code;
import com.xiaoyu.common.Result;
import com.xiaoyu.Dao.UserDao;
import com.xiaoyu.pojo.User;
import com.xiaoyu.dto.LoginDto;
import com.xiaoyu.dto.UserRegisterDto;
import com.xiaoyu.service.UserService;
import com.xiaoyu.utils.JwtUtil;
import com.xiaoyu.utils.UserContext;
import com.xiaoyu.vo.LoginVo;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class UserServiceimpl implements UserService {
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserDao userDao;
    @Resource
    private UserContext userContext;

    /*
    * 用户登录功能实现
    * */
    @Override
    public Result UserLogin(LoginDto loginDto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,loginDto.getUsername());
        User user = userDao.selectOne(queryWrapper);
        if (user == null){
            return new Result(Code.USER_NOT_EXIST,null,"用户不存在");
        }
        if(user.getStatus()==0){
            return new Result(Code.USER_STATUS_ERROR,null,"用户被禁用");
        }
        if (!passwordEncoder.matches(loginDto.getPassword(),user.getPassword())){
            return new Result(Code.PASSWORD_ERROR,null,"密码错误");
        }

        // ==========新增：重置登出时间，清空失效标记==========
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLogoutTime(null);
        userDao.updateById(updateUser);

        String token = jwtUtil.generateToken(user.getId(),user.getRole());
        LoginVo loginVo = new LoginVo();
        loginVo.setUserId(user.getId());
        loginVo.setUsername(user.getUsername());
        loginVo.setNickname(user.getNickname());
        loginVo.setRole(user.getRole());
        loginVo.setToken(token);
        return new Result(Code.SUCCESS,loginVo,"登录成功");
    }
    /*
    * 用户登出服务实现*/
    @Override
    public Result logout(String token) {
        String realToken =token.replace("Bearer ","");
        Claims claims = jwtUtil.parseToken(realToken);
        Long userid = (Long) claims.get("userid");
        //更新用户登出状态，标记token失效
        User updateuser = new User();
        updateuser.setId(userid);
        updateuser.setLogoutTime(LocalDateTime.now());
        userDao.updateById(updateuser);

        userContext.clear();
        return new Result(Code.SUCCESS,userid,"登出成功");

    }
    /*
    * 用户注册服务实现*/
    @Override
    public Result register(UserRegisterDto userRegisterDto) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,userRegisterDto.getUsername());
        User exitUser = userDao.selectOne(queryWrapper);
        if (exitUser != null){
            return new Result(Code.USERNAME_EXIST,null,"用户已存在");
        }
        //转实体类
        User user = new User();
        user.setUsername(userRegisterDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.setNickname(userRegisterDto.getNickname());
        user.setPhone(userRegisterDto.getPhone());
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(new Date());

        if(userDao.insert(user) > 0) {
            return new Result(Code.SUCCESS,userRegisterDto, "注册成功");
        }else{
            return new Result(Code.FAIL, null, "注册失败");
        }
    }
}
