package com.costinsight.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.costinsight.user.constant.RoleConstants;
import com.costinsight.user.dto.*;
import com.costinsight.user.entity.User;
import com.costinsight.user.mapper.UserMapper;
import com.costinsight.user.service.UserService;
import com.costinsight.user.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs; // JWT 过期时间 (毫秒)

    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder, UserMapper userMapper, JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserResponseVO register(RegisterRequest registerRequest) {
        // 1. 校验参数
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 2. 检查用户名和邮箱是否已存在
        if (userMapper.selectOne(
                new QueryWrapper<User>().eq("username", registerRequest.getUsername()).eq("deleted", 0)) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userMapper
                .selectOne(new QueryWrapper<User>().eq("email", registerRequest.getEmail()).eq("deleted", 0)) != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 3. 创建 User 对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setRole(RoleConstants.ROLE_USER);
        // 4. 密码加密
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0); // 未删除

        // 5. 保存到数据库
        userMapper.insert(user);

        // 6. 转换为 VO 并返回
        return convertToVO(user);
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
        String token = jwtUtil.generateToken(user);

        // 5. 返回 JwtResponse
        return new JwtResponse(token, jwtExpirationMs);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username).eq("deleted", 0));
    }

    @Override
    public UserResponseVO findUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            return null; // 或者抛出异常
        }
        return convertToVO(user);
    }

    @Override
    public UserResponseVO updateUserById(Long id, UserUpdateRequest updateRequest) {
        // 1. 根据ID查找用户
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("User not found");
        }

        // 2. 检查用户请求更新的邮箱是否与当前邮箱不同
        if (user.getEmail().equals(updateRequest.getEmail())) {
            // 如果邮箱相同，则提示用户邮箱相同
            throw new IllegalArgumentException("Email is the same");
        }

        // 3. 如果邮箱不同，则验证新邮箱的唯一性
        //    (确保新邮箱未被其他活跃用户占用，如果已占用则抛出业务异常)
        if (userMapper
                .selectOne(new QueryWrapper<User>().eq("email", updateRequest.getEmail()).eq("deleted", 0)) != null) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setEmail(updateRequest.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return convertToVO(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        // 1. 校验新密码: 首先检查 changePasswordRequest 中的 newPassword 和 confirmPassword
        // 是否一致，如果不一致则抛出异常。
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // 2. 获取用户: 根据 userId 从数据库中查询出对应的 User 实体。
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("User not found");
        }

        // 3. 验证旧密码: 使用 BCryptPasswordEncoder 的 matches 方法，验证用户提交的 oldPassword
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            // 是否与数据库中存储的加密密码匹配。这是最关键的安全校验。
            throw new IllegalArgumentException("Invalid old password");
        }

        // 4. 加密新密码: 如果旧密码验证通过，则使用 passwordEncoder.encode 方法对 newPassword 进行加密。
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        // 5. 更新数据库: 将加密后的新密码更新到 User 实体中，并将其保存回数据库。
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        // 1. 根据ID查找用户
        User user = userMapper.selectById(id);

        // 2. 校验用户是否存在且未被删除
        if (user == null || user.getDeleted() == 1) {
            throw new IllegalArgumentException("User not found or already deleted");
        }

        // 3. 执行逻辑删除
        // ServiceImpl<M, T> 提供了 removeById(id) 方法，它会自动处理 @TableLogic 注解
        this.removeById(id);
    }

    private UserResponseVO convertToVO(User user) {
        if (user == null) {
            return null;
        }
        UserResponseVO vo = new UserResponseVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    @Override
    public void logout(String token) {
        try {
            Claims claims = jwtUtil.parseTokenAndGetClaims(token);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            long remainingMillis = expiration.getTime() - System.currentTimeMillis();

            // 只有当 token 尚未过期时，才将其加入黑名单
            if (remainingMillis > 0) {
                String key = "jwt:blacklist:" + jti;
                redisTemplate.opsForValue().set(key, "1", remainingMillis, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 如果 token 解析失败（例如已过期或格式错误），我们无需做任何事，因为它已经无法通过验证。
            // 这里可以添加日志记录，用于调试。
            // log.warn("Could not add token to blacklist. It may be expired or invalid: {}", e.getMessage());
        }
    }
}