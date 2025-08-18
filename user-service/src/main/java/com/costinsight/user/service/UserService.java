package com.costinsight.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.costinsight.user.dto.JwtResponse;
import com.costinsight.user.dto.LoginRequest;
import com.costinsight.user.dto.RegisterRequest;
import com.costinsight.user.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param registerRequest 注册请求 DTO
     * @return User 注册成功的用户对象
     */
    User register(RegisterRequest registerRequest);

    /**
     * 用户登录
     * @param loginRequest 登录请求 DTO
     * @return JwtResponse 包含 JWT Token 的响应 DTO
     */
    JwtResponse login(LoginRequest loginRequest);
}