package com.costinsight.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应视图对象 (VO)
 * 用于向前端返回安全的、不包含密码等敏感信息的用户数据
 */
@Data
public class UserResponseVO {

    private Long id;

    private String username;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
