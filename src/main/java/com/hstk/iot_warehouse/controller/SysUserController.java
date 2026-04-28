package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.SysUser;
import com.hstk.iot_warehouse.service.SysUserService;
import com.hstk.iot_warehouse.common.utils.JwtUtils; // 导入 JwtUtils
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统用户管理控制器
 * 提供用户增删改查等接口
 */
@Slf4j
@RestController
@RequestMapping("/sys-users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 添加新用户
     * @param user 用户信息
     * @return 操作结果
     */
    @PostMapping
    public Result<String> add(@RequestBody SysUser user) {
        // verify username uniqueness, etc? for now just basic insert
        sysUserService.add(user);
        return Result.success("User added successfully");
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody SysUser user) {
        sysUserService.update(user);
        return Result.success("User updated successfully");
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        sysUserService.deleteById(id);
        return Result.success("User deleted successfully");
    }

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    @GetMapping
    public Result<List<SysUser>> getAll() {
        List<SysUser> list = sysUserService.getAll();
        return Result.success(list);
    }
}
