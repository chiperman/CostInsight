package com.costinsight.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求 DTO")
public class LoginRequest {

    @Schema(description = "用户名或邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "testuser")
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Password123")
    @NotBlank(message = "Password is required")
    private String password;
}
