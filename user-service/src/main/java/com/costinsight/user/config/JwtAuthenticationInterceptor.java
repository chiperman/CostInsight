package com.costinsight.user.config;

import com.costinsight.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseTokenAndGetClaims(token);

                // 1. 从 Claims 中获取 JTI
                String jti = claims.getId();
                if (jti == null) {
                    throw new JwtException("Token does not have a JTI (JWT ID).");
                }

                // 2. 构造 Redis Key
                String redisKey = "jwt:blacklist:" + jti;

                // 3. 检查该 Key 是否存在于 Redis 中
                Boolean isBlacklisted = redisTemplate.hasKey(redisKey);

                // 4. 如果 isBlacklisted 为 true，说明 token 已登出，拒绝请求
                if (isBlacklisted != null && isBlacklisted) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been blacklisted and cannot be used.");
                    return false;
                }

                // Token 有效且不在黑名单中，将 userId 放入 request attribute，允许访问
                Long userId = Long.parseLong(claims.getSubject());
                request.setAttribute("userId", userId);
                return true;

            } catch (Exception e) {
                // 捕获所有 JWT 相关的异常 (如过期、格式错误、JTI为空等)
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
                return false;
            }
        }

        // 对于需要认证的接口，如果 Token 缺失，则拒绝访问
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Missing or invalid Authorization header.");
        return false;
    }

    /**
     * 发送统一的错误响应
     * @param response HttpServletResponse 对象
     * @param status HTTP 状态码
     * @param message 错误信息
     * @throws IOException IO 异常
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", status);
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}