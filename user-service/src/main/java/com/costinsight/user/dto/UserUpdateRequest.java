package com.costinsight.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户更新信息请求 DTO")
public class UserUpdateRequest {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "new.email@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
