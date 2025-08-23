package com.costinsight.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.costinsight.user.dto.ChangePasswordRequest;
import com.costinsight.user.dto.JwtResponse;
import com.costinsight.user.dto.LoginRequest;
import com.costinsight.user.dto.RegisterRequest;
import com.costinsight.user.dto.UserResponseVO;
import com.costinsight.user.dto.UserUpdateRequest;
import com.costinsight.user.entity.User;
import com.costinsight.user.mapper.UserMapper;
import com.costinsight.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(userService, "jwtExpirationMs", 86400000L);
    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // When
        UserResponseVO result = userService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userMapper, times(2)).selectOne(any(QueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void testUpdateUserById_Success() {
        // Given
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("new@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        when(userMapper.selectById(userId)).thenReturn(existingUser);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // When
        UserResponseVO result = userService.updateUserById(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("new@example.com", result.getEmail());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testFindUserById_Success() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("test");

        when(userMapper.selectById(userId)).thenReturn(user);

        // When
        UserResponseVO result = userService.findUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test", result.getUsername());
    }

    @Test
    void testChangePassword_Success() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmNewPassword("newPass");

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPass");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // When
        userService.changePassword(userId, request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(userCaptor.capture());
        assertEquals("encodedNewPass", userCaptor.getValue().getPassword());
    }

    @Test
    void testChangePassword_OldPasswordMismatch() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPass");
        request.setNewPassword("newPass");
        request.setConfirmNewPassword("newPass");

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPass");

        when(userMapper.selectById(userId)).thenReturn(user);
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPass")).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(userId, request);
        });
        verify(userMapper, never()).updateById(any(User.class));
    }
}
