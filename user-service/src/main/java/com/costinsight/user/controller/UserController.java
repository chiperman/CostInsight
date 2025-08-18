package com.costinsight.user.controller;

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

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理接口", description = "用户信息的增删改查接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "创建用户", description = "创建一个新的用户（示例接口，实际注册请使用 /api/auth/register）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "500", description = "创建失败",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        boolean saved = userService.save(user);
        if (saved) {
            return ResponseUtil.success("User created successfully", ResponseStatus.SUCCESS);
        } else {
            return ResponseUtil.error(ResponseStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }

    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "404", description = "用户未找到",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Long id) {
        System.out.println("Getting user with id: " + id);
        User user = userService.getById(id);
        if (user != null) {
            return ResponseUtil.success(user, ResponseStatus.SUCCESS);
        } else {
            return ResponseUtil.error(ResponseStatus.USER_NOT_FOUND, "User not found with id: " + id);
        }
    }
}