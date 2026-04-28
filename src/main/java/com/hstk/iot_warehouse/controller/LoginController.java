package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.common.utils.JwtUtils;
import com.hstk.iot_warehouse.model.entity.SysUser;
import com.hstk.iot_warehouse.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录接口
     * @param user 用户名和密码
     * @return 包含 token 的结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody SysUser user) {
        log.info("user login: {}", user.getUsername());
        SysUser loginUser = sysUserService.login(user); // Reuse SysUserService
        //登陆成功
        if (loginUser != null) {
            Map<String, Object> claims = new HashMap<>();
            //添加claims
            claims.put("id", loginUser.getUserId());
            claims.put("username", loginUser.getUsername());
            claims.put("role", loginUser.getRole() != null ? loginUser.getRole() : "user");
            
            String token = JwtUtils.generateJwt(claims);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", loginUser); // 也可以返回用户基本信息
            
            return Result.success(data);
        }
        return Result.error("用户名或密码错误");
    }
}
