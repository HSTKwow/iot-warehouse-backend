package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存实物实体类 (One Record = One RFID Tag)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockItem {
    /** 记录ID */
    private Long itemId;
    /** RFID标签号 */
    private String rfidTag;
    /** 关联物资资料ID */
    private Long materialId;
    /** 数量 (如 1箱, 或 500g) */
    private BigDecimal quantity;
    /** 批次号 */
    private String batchNo;
    /** 有效期 */
    private LocalDate expiryDate;
    /** 存放位置 (仓库-货架-位) */
    private String location;
    /** 状态 (1:在库, 0:出库, 2:损耗, 4:过期) */
    private Integer status;
    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime inTime;
}
