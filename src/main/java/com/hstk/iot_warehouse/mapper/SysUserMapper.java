package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 系统用户Mapper接口
 */
@Mapper
public interface SysUserMapper {
    /**
     * 插入用户
     * @param user 用户对象
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新用户
     * @param user 用户对象
     * @return 影响行数
     */
    int update(SysUser user);

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(Long userId);

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户对象
     */
    SysUser selectById(Long userId);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    SysUser selectByUsername(String username);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<SysUser> selectAll();

    /**
     * 更新密码
     * @param userId 用户ID
     * @param password 新密码
     * @return 影响行数
     */
    int updatePassword(Long userId, String password);

    /**
     * 更新状态
     * @param userId 用户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(Long userId, Integer status);
}