package com.costinsight.user.util;

import com.costinsight.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}")
    private String jwtSecretString;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try {
            // 优先尝试 Base64 解码
            keyBytes = Base64.getDecoder().decode(jwtSecretString);
        } catch (IllegalArgumentException e) {
            // 如果解码失败，则视为普通字符串
            keyBytes = jwtSecretString.getBytes(StandardCharsets.UTF_8);
        }

        // 确保密钥长度至少为 512 位（64 字节），这是 HS512 算法的要求
        if (keyBytes.length < 64) {
            logger.warn("Warning: The configured JWT secret is shorter than 64 bytes. " +
                    "It is highly recommended to use a longer, Base64-encoded secret for production.");
            // 为了保持密钥的确定性，我们使用填充或派生方式，而不是完全随机生成
            byte[] newKeyBytes = new byte[64];
            System.arraycopy(keyBytes, 0, newKeyBytes, 0, Math.min(keyBytes.length, 64));
            keyBytes = newKeyBytes;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取用于签名的 SecretKey
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        return this.secretKey;
    }

    /**
     * 生成 JWT Token
     *
     * @param user 用户实体
     * @return String JWT Token
     */
    public String generateToken(User user) {
        // 获取签名密钥
        SecretKey key = getSigningKey();

        // 设置过期时间
        Date expiryDate = new Date((new Date()).getTime() + jwtExpirationMs);

        // 构建并返回 JWT Token
        return Jwts.builder()
                .setSubject(user.getId().toString()) // 设置主题为用户ID
                .setId(UUID.randomUUID().toString()) // 设置 JTI，Token 的唯一标识
                .claim("username", user.getUsername()) // 添加 username
                .claim("email", user.getEmail())       // 添加 email
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(expiryDate) // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS512) // 使用 HS512 算法和密钥签名
                .compact(); // 生成紧凑的 JWT 字符串
    }

    /**
     * 验证 JWT Token
     *
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
     * 从 JWT Token 中获取用户ID (Subject)
     *
     * @param token JWT Token
     * @return String 用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseTokenAndGetClaims(token).getSubject();
    }

    /**
     * 从 JWT Token 中获取用户名
     *
     * @param token JWT Token
     * @return String 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseTokenAndGetClaims(token).get("username", String.class);
    }

    /**
     * 从 JWT Token 中获取邮箱
     *
     * @param token JWT Token
     * @return String 邮箱
     */
    public String getEmailFromToken(String token) {
        return parseTokenAndGetClaims(token).get("email", String.class);
    }

    /**
     * 解析 Token 并返回所有载荷信息 (Claims)
     * <p>
     * 如果 Token 无效（过期、签名错误等），此方法会抛出相应的 JwtException。
     *
     * @param token JWT Token
     * @return Claims 载荷信息
     * @throws ExpiredJwtException      如果 Token 过期
     * @throws MalformedJwtException    如果 Token 格式错误
     * @throws SecurityException        如果 Token 签名无效
     * @throws IllegalArgumentException 如果参数错误
     */
    public Claims parseTokenAndGetClaims(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}