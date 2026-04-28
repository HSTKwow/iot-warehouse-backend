package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser {
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 状态 1:正常 0:禁用 */
    private Integer status;
    /** 角色 (admin/user) */
    private String role;
    /** 创建时间 */
    private LocalDateTime createTime;
}
