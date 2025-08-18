package com.costinsight.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.costinsight.user.dto.JwtResponse;
import com.costinsight.user.dto.LoginRequest;
import com.costinsight.user.dto.RegisterRequest;
import com.costinsight.user.entity.User;
import com.costinsight.user.mapper.UserMapper;
import com.costinsight.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    private final String jwtSecret = "mySecretKeyForCostInsightApplicationWhichIsLongEnoughToBeSecure";
    private final long jwtExpirationMs = 86400000;

    @BeforeEach
    void setUp() {
        // 使用 ReflectionTestUtils 设置私有字段
        ReflectionTestUtils.setField(userService, "jwtExpirationMs", jwtExpirationMs);
    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null); // 模拟用户不存在
        when(userMapper.insert(any(User.class))).thenReturn(1); // 模拟插入成功

        // When
        User result = userService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertNull(result.getPassword()); // 密码不应返回
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(0, result.getDeleted());

        // 验证方法调用
        verify(userMapper, times(2)).selectOne(any(QueryWrapper.class)); // 检查用户名和邮箱
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testRegister_PasswordMismatch() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("differentpassword");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userMapper, never()).selectOne(any(QueryWrapper.class));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("newemail@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(existingUser); // 模拟用户名已存在

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class)); // 只检查用户名
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(new BCryptPasswordEncoder().encode("password123")); // 模拟加密后的密码

        String token = "generated-jwt-token";
        when(userMapper.selectByUsername("testuser")).thenReturn(user);
        when(jwtUtil.generateToken("testuser")).thenReturn(token);

        // When
        JwtResponse result = userService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(token, result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(jwtExpirationMs, result.getExpiresIn());

        // 验证方法调用
        verify(userMapper, times(1)).selectByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("nonexistentuser");
        loginRequest.setPassword("password123");

        when(userMapper.selectByUsername("nonexistentuser")).thenReturn(null);
        when(userMapper.selectByEmail("nonexistentuser")).thenReturn(null); // 模拟用户不存在

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userMapper, times(1)).selectByUsername("nonexistentuser");
        verify(userMapper, times(1)).selectByEmail("nonexistentuser");
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(new BCryptPasswordEncoder().encode("correctpassword")); // 模拟正确的加密密码

        when(userMapper.selectByUsername("testuser")).thenReturn(user);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid password", exception.getMessage());
        verify(userMapper, times(1)).selectByUsername("testuser");
        verify(jwtUtil, never()).generateToken(anyString());
    }
}