package com.costinsight.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.costinsight.user.dto.JwtResponse;
import com.costinsight.user.dto.LoginRequest;
import com.costinsight.user.dto.RegisterRequest;
import com.costinsight.user.entity.User;
import com.costinsight.user.mapper.UserMapper;
import com.costinsight.user.service.UserService;
import com.costinsight.user.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs; // JWT 过期时间 (毫秒)

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(RegisterRequest registerRequest) {
        // 1. 校验参数
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 2. 检查用户名和邮箱是否已存在
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", registerRequest.getUsername()).eq("deleted", 0)) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper.selectOne(new QueryWrapper<User>().eq("email", registerRequest.getEmail()).eq("deleted", 0)) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 3. 创建 User 对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        // 4. 密码加密
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0); // 未删除

        // 5. 保存到数据库
        userMapper.insert(user);

        // 6. 返回用户对象 (不包含密码)
        user.setPassword(null);
        return user;
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        // 1. 根据用户名或邮箱查找用户
        User user = userMapper.selectByUsername(loginRequest.getUsernameOrEmail());
        if (user == null) {
            user = userMapper.selectByEmail(loginRequest.getUsernameOrEmail());
        }

        // 2. 校验用户是否存在且未被删除
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("User not found");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // 4. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getUsername());

        // 5. 返回 JwtResponse
        return new JwtResponse(token, jwtExpirationMs);
    }
}