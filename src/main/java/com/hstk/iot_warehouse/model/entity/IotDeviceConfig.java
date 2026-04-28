package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 设备阈值配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceConfig {
    /** 配置ID */
    private Long configId;
    /** 关联设备ID */
    private String deviceId;
    /** 温度上限阈值 */
    private BigDecimal tempMax;
    /** 温度下限阈值 */
    private BigDecimal tempMin;
    /** 湿度上限阈值 */
    private BigDecimal humidityMax;
    /** 烟雾浓度告警阈值 */
    private BigDecimal smokeThreshold;
    /** 气压上限阈值 */
    private BigDecimal pressureMax;
    /** 露点上限阈值 */
    private BigDecimal dewPointMax;
    /** 光照上限阈值(Lux) */
    private BigDecimal luxMax;
    /** 工作模式 (inbound/outbound/both) */
    private String workMode;
    /** 报警模式 (auto/manual) */
    private String alarmMode;
}
