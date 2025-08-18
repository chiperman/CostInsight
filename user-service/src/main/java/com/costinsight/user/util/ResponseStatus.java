package com.costinsight.user.util;

/**
 * 响应状态码和消息的枚举
 */
public enum ResponseStatus {
    // 成功
    SUCCESS(200, "Success"),

    // 用户相关错误
    USER_REGISTERED_SUCCESS(200, "User registered successfully"),
    LOGIN_SUCCESS(200, "Login successful"),
    INVALID_CREDENTIALS(401, "Invalid credentials"),
    USER_NOT_FOUND(404, "User not found"),
    USER_ALREADY_EXISTS(400, "User already exists"),
    PASSWORD_MISMATCH(400, "Passwords do not match"),

    // 通用错误
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    REGISTRATION_ERROR(500, "An error occurred during registration"),
    LOGIN_ERROR(500, "An error occurred during login");

    private final int code;
    private final String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}