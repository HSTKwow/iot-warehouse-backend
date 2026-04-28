package com.hstk.iot_warehouse.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 物联网设备实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDevice {
    /** 设备ID (如 MAC地址或UUID) */
    private String deviceId;
    /** 设备名称 */
    private String deviceName;
    /** 设备类型 (Sensor, Gateway, Camera ...) */
    private String deviceType;
    /** 安装位置 */
    private String location;
    /** 设备状态 (1:在线, 0:离线) */
    private Integer status;
    /** 如果是个人设备，关联所有者 */
    private Long ownerId;
    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // --- Transient Fields (Not in DB) ---
    private Double temperature;
    private Double humidity;
    private Boolean ledStatus;
    private Boolean alarmStatus;
}
