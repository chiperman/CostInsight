package com.costinsight.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改密码请求 DTO")
public class ChangePasswordRequest {

    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "OldPassword123")
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "NewPassword123")
    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}",
        message = "Password must be at least 8 characters long, and contain at least one uppercase letter, one lowercase letter, and one number."
    )
    private String newPassword;

    @Schema(description = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "NewPassword123")
    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}
