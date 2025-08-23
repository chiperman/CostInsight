package com.costinsight.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Token 验证请求 DTO")
public class ValidateRequest {

    @Schema(description = "待验证的JWT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Token cannot be blank")
    private String token;

}