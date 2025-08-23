package com.costinsight.user.controller;

import com.costinsight.user.dto.ChangePasswordRequest;
import com.costinsight.user.dto.UserResponseVO;
import com.costinsight.user.dto.UserUpdateRequest;
import com.costinsight.user.service.UserService;
import com.costinsight.user.util.ResponseStatus;
import com.costinsight.user.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "用户信息的增删改查接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的邮箱等信息",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseVO.class))}),
            @ApiResponse(responseCode = "400", description = "请求参数错误或邮箱已存在",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "未授权",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "用户未找到",
                    content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<?> updateUser(@RequestAttribute("userId") Long userId, @Valid @RequestBody UserUpdateRequest updateRequest) {
        try {
            // 更新用户信息
            UserResponseVO updatedUser = userService.updateUserById(userId, updateRequest);
            return ResponseUtil.success(updatedUser, ResponseStatus.SUCCESS);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(ResponseStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(ResponseStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the user.");
        }
    }


    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseVO.class))}),
            @ApiResponse(responseCode = "404", description = "用户未找到",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        UserResponseVO userVO = userService.findUserById(id);
        if (userVO != null) {
            return ResponseUtil.success(userVO, ResponseStatus.SUCCESS);
        } else {
            return ResponseUtil.error(ResponseStatus.USER_NOT_FOUND, "User not found with id: " + id);
        }
    }


    @Operation(summary = "修改当前用户密码", description = "修改当前登录用户的密码",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "密码更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误（如旧密码错误）"),
            @ApiResponse(responseCode = "401", description = "未授权"),
            @ApiResponse(responseCode = "404", description = "用户未找到")
    })
    @PutMapping("/me/password")
    public ResponseEntity<?> changeCurrentUserPassword(@RequestAttribute("userId") Long userId, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            userService.changePassword(userId, changePasswordRequest);
            return ResponseUtil.success(null, ResponseStatus.PASSWORD_UPDATED_SUCCESS);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(ResponseStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.error(ResponseStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the password.");
        }
    }

    @Operation(summary = "删除当前用户（逻辑删除）", description = "删除当前登录的用户。注意：这是一个软删除操作。",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未授权"),
            @ApiResponse(responseCode = "404", description = "用户未找到")
    })
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteSelf(@RequestAttribute("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseUtil.success(null, ResponseStatus.SUCCESS);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(ResponseStatus.USER_NOT_FOUND, e.getMessage());
        }
    }

}