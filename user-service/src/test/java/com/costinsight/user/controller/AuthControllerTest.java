package com.costinsight.user.controller;

import com.costinsight.user.dto.JwtResponse;
import com.costinsight.user.dto.LoginRequest;
import com.costinsight.user.dto.RegisterRequest;
import com.costinsight.user.dto.UserResponseVO;
import com.costinsight.user.service.UserService;
import com.costinsight.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 的集成测试类，用于验证用户注册和登录接口的行为。
 * 使用 @WebMvcTest 注解启动 Spring MVC 测试环境，仅加载控制器层相关组件。
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    /**
     * MockMvc 实例，用于模拟 HTTP 请求并验证响应结果。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 模拟 UserService，用于替换真实的服务实现以控制测试行为。
     */
    @MockBean
    private UserService userService;

    /**
     * 模拟 JwtUtil，用于替换真实的 JWT 工具类实现。
     */
    @MockBean
    private JwtUtil jwtUtil;

    /**
     * ObjectMapper 实例，用于将 Java 对象序列化为 JSON 字符串。
     */
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 测试用户注册成功的情况。
     * 构造一个有效的 RegisterRequest 请求体，并模拟 userService 返回已注册的用户对象。
     * 验证返回状态码为 200，以及响应体中包含正确的用户信息。
     *
     * @throws Exception 如果在执行 MockMvc 请求时发生异常
     */
    @Test
    void testRegisterUser_Success() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setConfirmPassword("Password123");

        UserResponseVO registeredUserVO = new UserResponseVO();
        registeredUserVO.setId(1L);
        registeredUserVO.setUsername("testuser");
        registeredUserVO.setEmail("test@example.com");

        when(userService.register(any(RegisterRequest.class))).thenReturn(registeredUserVO);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    /**
     * 测试用户注册时密码不一致的情况。
     * 构造一个密码与确认密码不同的 RegisterRequest 请求体，
     * 并模拟 userService 抛出 IllegalArgumentException 异常。
     * 验证返回状态码为 400，以及响应体中包含错误信息。
     *
     * @throws Exception 如果在执行 MockMvc 请求时发生异常
     */
    @Test
    void testRegisterUser_PasswordMismatch() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setConfirmPassword("DifferentPassword123");

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Passwords do not match"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    /**
     * 测试用户登录成功的情况。
     * 构造一个有效的 LoginRequest 请求体，并模拟 userService 返回 JwtResponse。
     * 验证返回状态码为 200，以及响应体中包含 JWT token 和类型。
     *
     * @throws Exception 如果在执行 MockMvc 请求时发生异常
     */
    @Test
    void testAuthenticateUser_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("Password123");

        String token = "generated-jwt-token";
        long expiresIn = 86400000;
        JwtResponse jwtResponse = new JwtResponse(token, expiresIn);


        when(userService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value(token))
                .andExpect(jsonPath("$.data.type").value("Bearer"));
    }

    /**
     * 测试用户登录时凭据无效的情况。
     * 构造一个错误密码的 LoginRequest 请求体，
     * 并模拟 userService 抛出 IllegalArgumentException 异常。
     * 验证返回状态码为 401，以及响应体中包含错误信息。
     *
     * @throws Exception 如果在执行 MockMvc 请求时发生异常
     */
    @Test
    void testAuthenticateUser_InvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("WrongPassword123");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid password"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }
}
