package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 物联网告警实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotAlarm {
    /** 告警ID */
    private Long alarmId;
    /** 设备ID */
    private String deviceId;
    /** 告警类型 */
    private String alarmType;
    /** 严重程度 (1:一般, 2:严重, 3:紧急) */
    private Integer severity;
    /** 告警内容 */
    private String content;
    /** 状态 (0:未处理, 1:已处理) */
    private Integer status;
    /** 关联ID (如传感器数据ID) */
    private String relatedId;
    /** 告警来源 */
    private String alarmSource;
    /** 告警时间 */
    private LocalDateTime createTime;
    /** 处理时间 */
    private LocalDateTime resolveTime;
    /** 处理备注(采取的措施) */
    private String resolveRemark;
    /** 处理人 */
    private String resolveUser;
}
