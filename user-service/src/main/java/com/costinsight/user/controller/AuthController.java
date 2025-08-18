package com.costinsight.user.controller;

import com.costinsight.user.dto.JwtResponse;
import com.costinsight.user.dto.LoginRequest;
import com.costinsight.user.dto.RegisterRequest;
import com.costinsight.user.entity.User;
import com.costinsight.user.service.UserService;
import com.costinsight.user.util.ResponseStatus;
import com.costinsight.user.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "用户注册与登录相关接口")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * @param registerRequest 注册请求 DTO
     * @return ResponseEntity 注册结果
     */
    @Operation(summary = "用户注册", description = "使用用户名、邮箱和密码进行用户注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "请求参数错误",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User registeredUser = userService.register(registerRequest);
            return ResponseUtil.success(registeredUser, ResponseStatus.USER_REGISTERED_SUCCESS);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(ResponseStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(ResponseStatus.REGISTRATION_ERROR);
        }
    }

    /**
     * 用户登录接口
     * @param loginRequest 登录请求 DTO
     * @return ResponseEntity 登录结果 (包含 JWT Token)
     */
    @Operation(summary = "用户登录", description = "使用用户名/邮箱和密码进行登录，获取 JWT Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "凭证无效",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "服务器内部错误",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = userService.login(loginRequest);
            return ResponseUtil.success(jwtResponse, ResponseStatus.LOGIN_SUCCESS);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(ResponseStatus.INVALID_CREDENTIALS, e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(ResponseStatus.LOGIN_ERROR);
        }
    }
}