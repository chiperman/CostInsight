package com.costinsight.user.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // 注入 JWT 密钥
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // 注入 JWT 过期时间 (毫秒)
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;
    
    /**
     * 获取用于签名的 SecretKey
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        // 如果密钥长度足够，直接使用；否则使用Base64解码或生成安全密钥
        byte[] keyBytes;
        try {
            // 尝试将密钥作为Base64编码的字符串解码
            keyBytes = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            // 如果解码失败，使用密钥生成方法创建安全密钥
            keyBytes = jwtSecret.getBytes();
        }
        
        // 确保密钥长度足够安全
        if (keyBytes.length < 64) {
            // 如果密钥太短，使用密钥派生方法
            return Keys.hmacShaKeyFor(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     * @param username 用户名
     * @return String JWT Token
     */
    public String generateToken(String username) {
        // 获取签名密钥
        SecretKey key = getSigningKey();

        // 设置过期时间
        Date expiryDate = new Date((new Date()).getTime() + jwtExpirationMs);

        // 构建并返回 JWT Token
        return Jwts.builder()
                .setSubject(username) // 设置主题（通常是用户名）
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(expiryDate) // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS512) // 使用 HS512 算法和密钥签名
                .compact(); // 生成紧凑的 JWT 字符串
    }

    /**
     * 验证 JWT Token
     * @param token JWT Token
     * @return boolean 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 获取签名密钥
            SecretKey key = getSigningKey();

            // 解析 Token
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 如果没有抛出异常，则 Token 有效
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        return false; // Token 无效
    }

    /**
     * 从 JWT Token 中获取用户名
     * @param token JWT Token
     * @return String 用户名
     */
    public String getUsernameFromToken(String token) {
        // 获取签名密钥
        SecretKey key = getSigningKey();

        // 解析 Token 并获取主体（用户名）
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // 返回主体（用户名）
    }
}