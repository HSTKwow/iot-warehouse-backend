package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.SysUser;
import java.util.List;

/**
 * 系统用户服务接口
 */
public interface SysUserService {
    /**
     * 添加用户
     * @param user 用户信息
     * @return 影响行数
     */
    int add(SysUser user);

    /**
     * 更新用户
     * @param user 用户信息
     * @return 影响行数
     */
    int update(SysUser user);

    /**
     * 根据ID删除用户
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(Long userId);

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUser getById(Long userId);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 登录
     * @param user 用户对象
     * @return 登录成功返回用户，失败返回null
     */
    SysUser login(SysUser user);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<SysUser> getAll();

    /**
     * 修改密码
     * @param userId 用户ID
     * @param password 新密码
     * @return 影响行数
     */
    int updatePassword(Long userId, String password);

    /**
     * 修改状态
     * @param userId 用户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(Long userId, Integer status);
}
