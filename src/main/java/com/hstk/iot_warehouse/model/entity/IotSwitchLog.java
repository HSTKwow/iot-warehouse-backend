package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备开关/控制日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotSwitchLog {
    /** 日志ID */
    private Long logId;
    /** 设备ID */
    private String deviceId;
    /** 组件名称 (如 Light, Fan) */
    private String componentName;
    /** 动作 (ON, OFF) */
    private String action;
    /** 状态 (1:成功, 0:失败) */
    private Integer status;
    /** 触发来源 (Auto, Manual, App) */
    private String triggerSource;
    /** 操作人ID */
    private Long userId;
    /** 操作人名称(非DB字段，用于关联显示) */
    private String nickname; 
    /** 操作时间 */
    private LocalDateTime createTime;
}
