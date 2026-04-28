package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.SysUserMapper;
import com.hstk.iot_warehouse.model.entity.SysUser;
import com.hstk.iot_warehouse.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务实现类
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 添加用户
     * 自动设置创建时间和默认状态
     */
    @Override
    public int add(SysUser user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(LocalDateTime.now());
        }
        // default status to 1 if not set
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return sysUserMapper.insert(user);
    }

    @Override
    public int update(SysUser user) {
        return sysUserMapper.update(user);
    }

    @Override
    public int deleteById(Long userId) {
        return sysUserMapper.deleteById(userId);
    }

    @Override
    public SysUser getById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    /**
     * 登录逻辑
     * @param user 用户提交的登录信息
     * @return 登录成功的用户对象
     */
    @Override
    public SysUser login(SysUser user) {
        SysUser dbUser = sysUserMapper.selectByUsername(user.getUsername());
        if (dbUser == null) {
            return null; // 用户不存在
        }
        // 简单比对明文密码 (生产环境建议加密比对)
        if (dbUser.getPassword().equals(user.getPassword())) {
            return dbUser;
        }
        return null; // 密码错误
    }

    @Override
    public List<SysUser> getAll() {
        return sysUserMapper.selectAll();
    }

    @Override
    public int updatePassword(Long userId, String password) {
        return sysUserMapper.updatePassword(userId, password);
    }

    @Override
    public int updateStatus(Long userId, Integer status) {
        return sysUserMapper.updateStatus(userId, status);
    }
}
