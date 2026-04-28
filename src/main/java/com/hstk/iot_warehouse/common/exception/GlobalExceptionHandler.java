package com.hstk.iot_warehouse.common.exception;

import com.hstk.iot_warehouse.common.api.Result;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex){
        log.error("Global Exception: ", ex);
        return new Result<>(0, "操作失败: " + ex.getMessage(), null);
    }

    /**
     * 捕获数据库唯一键冲突异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException ex){
        log.error("Duplicate Key Exception: ", ex);
        return new Result<>(0, "数据已存在，请勿重复添加", null);
    }
    
    /**
     * 捕获 JWT 相关异常 (未登录或 Token 失效)
     */
    @ExceptionHandler(JwtException.class)
    public Result<String> handleJwtException(JwtException ex, HttpServletResponse response){
        log.error("JWT Exception: ", ex);
        // 设置 HTTP 状态码为 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new Result<>(0, "NOT_LOGIN", null);
    }
}
