package com.costinsight.user.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String jwtSecret = "mySecretKeyForCostInsightApplicationWhichIsLongEnoughToBeSecureAndEvenLongerToMeetRequirements";
    private final int jwtExpirationMs = 86400000; // 24小时

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 使用 ReflectionTestUtils 设置私有字段
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", jwtExpirationMs);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken_ValidToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.string";

        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // 创建一个已过期的 Token
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - jwtExpirationMs - 1000)) // 过期时间在很久以前
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1秒前过期
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertEquals(username, jwtUtil.getUsernameFromToken(token));
    }
}