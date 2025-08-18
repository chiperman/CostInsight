package com.costinsight.user.util;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    /**
     * 构建成功响应
     * @param data 响应数据
     * @param status 响应状态
     * @return ResponseEntity 成功响应
     */
    public static ResponseEntity<?> success(Object data, ResponseStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.getCode());
        response.put("message", status.getMessage());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * 构建成功响应（使用默认成功状态）
     * @param data 响应数据
     * @return ResponseEntity 成功响应
     */
    public static ResponseEntity<?> success(Object data) {
        return success(data, ResponseStatus.SUCCESS);
    }

    /**
     * 构建错误响应
     * @param status 错误状态
     * @return ResponseEntity 错误响应
     */
    public static ResponseEntity<?> error(ResponseStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.getCode());
        response.put("message", status.getMessage());
        response.put("data", null);
        return ResponseEntity.status(getHttpStatus(status.getCode())).body(response);
    }
    
    /**
     * 构建带自定义消息的错误响应
     * @param status 错误状态
     * @param message 自定义错误消息
     * @return ResponseEntity 错误响应
     */
    public static ResponseEntity<?> error(ResponseStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.getCode());
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(getHttpStatus(status.getCode())).body(response);
    }

    /**
     * 根据错误码获取HTTP状态码
     * @param code 错误码
     * @return int HTTP状态码
     */
    private static int getHttpStatus(int code) {
        switch (code) {
            case 400:
                return 400; // BAD_REQUEST
            case 401:
                return 401; // UNAUTHORIZED
            case 403:
                return 403; // FORBIDDEN
            case 404:
                return 404; // NOT_FOUND
            default:
                return 500; // INTERNAL_SERVER_ERROR
        }
    }
}