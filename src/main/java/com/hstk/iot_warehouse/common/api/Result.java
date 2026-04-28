package com.hstk.iot_warehouse.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * 用于前后端交互时统一返回格式
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code; // 状态码，例如 1:成功, 0:失败
    private String msg;   // 提示信息
    private T data;       // 返回数据

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(1, "success", data);
    }
    
    /**
     * 成功响应（不带数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(1, "success", null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
    }
}
