package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 传感器数据实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotSensorData {
    /** 数据ID */
    private Long dataId;
    /** 设备ID */
    private String deviceId;
    /** 温度 */
    private BigDecimal temperature;
    /** 湿度 */
    private BigDecimal humidity;
    /** 气压 */
    private BigDecimal pressure;
    /** 烟雾浓度 */
    private BigDecimal smokeDensity;
    /** 露点(可选) */
    private BigDecimal dewPoint;
    /** 光照强度(Lux) */
    private BigDecimal luminance;
    /** 额外信息(JSON格式) */
    private String extraInfo; 
    /** 上报时间 */
    private LocalDateTime reportTime;
}
