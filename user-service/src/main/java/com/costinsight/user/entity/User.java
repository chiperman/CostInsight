package com.costinsight.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user") // 指定数据库表名
public class User {

    @TableId(value = "id", type = IdType.AUTO) // 主键，自动增长
    private Long id;

    @TableField("username") // 用户名
    private String username;

    @TableField("email") // 邮箱
    private String email;

    @TableField("password") // 密码
    private String password;

    @TableField("role") // 角色
    private String role;

    @TableField(value = "created_at", fill = FieldFill.INSERT) // 创建时间，插入时自动填充
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE) // 更新时间，插入和更新时自动填充
    private LocalDateTime updatedAt;

    @TableField("deleted") // 逻辑删除标识
    @TableLogic // MyBatis-Plus 逻辑删除注解
    private Integer deleted = 0; // 0: 未删除, 1: 已删除
}