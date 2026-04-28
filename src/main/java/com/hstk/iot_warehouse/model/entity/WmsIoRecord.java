package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 出入库流水记录实体类
 * 核心：通过RFID关联
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsIoRecord {
    /** 记录ID */
    private Long recordId;
    /** RFID标签号 (Unique Tag ID) */
    private String rfidTag;
    /** 类型 (1:入库, 2:出库) */
    private Integer ioType;
    /** 关联订单号/业务单号 */
    private String orderNo;
    /** 扫描设备ID */
    private String deviceId;
    /** 操作时间 */
    private LocalDateTime time;
}
