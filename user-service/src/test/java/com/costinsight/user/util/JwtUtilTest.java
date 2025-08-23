package com.costinsight.user.util;

import com.costinsight.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String jwtSecretString = "mySecretKeyForCostInsightApplicationWhichIsLongEnoughToBeSecureAndEvenLongerToMeetRequirements";
    private final long jwtExpirationMs = 86400000L; // 24 hours

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 使用 ReflectionTestUtils 设置私有字段
        ReflectionTestUtils.setField(jwtUtil, "jwtSecretString", jwtSecretString);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", jwtExpirationMs);
        jwtUtil.init(); // 在测试环境中手动调用 @PostConstruct 方法

        // 为测试创建一个用户样本
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtUtil.generateToken(testUser);
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
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1L); // 将过期时间设置为过去
        String expiredToken = jwtUtil.generateToken(testUser);
        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken(testUser);
        assertEquals(testUser.getId().toString(), jwtUtil.getUserIdFromToken(token));
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken(testUser);
        assertEquals(testUser.getUsername(), jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void testGetEmailFromToken() {
        String token = jwtUtil.generateToken(testUser);
        assertEquals(testUser.getEmail(), jwtUtil.getEmailFromToken(token));
    }
}
