package com.costinsight.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.costinsight.user.dto.*;
import com.costinsight.user.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求 DTO
     * @return UserResponseVO 注册成功的用户VO
     */
    UserResponseVO register(RegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求 DTO
     * @return JwtResponse 包含 JWT Token 的响应 DTO
     */
    JwtResponse login(LoginRequest loginRequest);

    /**
     * 根据用户名查找用户实体（内部使用）
     *
     * @param username 用户名
     * @return User 用户对象
     */
    User findByUsername(String username);

    /**
     * 根据ID安全地查找用户信息
     *
     * @param id 用户ID
     * @return UserResponseVO 用户VO
     */
    UserResponseVO findUserById(Long id);


    /**
     * 根据ID更新用户信息
     *
     * @param id            用户ID
     * @param updateRequest 更新请求 DTO
     * @return 更新后的用户VO
     * @throws IllegalArgumentException 如果用户不存在或邮箱已存在
     */
    UserResponseVO updateUserById(Long id, UserUpdateRequest updateRequest);

    /**
     * 根据ID修改密码
     *
     * @param userId 用户ID
     * @param changePasswordRequest 修改密码请求 DTO
     */
    void changePassword(Long userId, ChangePasswordRequest changePasswordRequest);

    /**
     * 根据ID删除用户（逻辑删除）
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);
}