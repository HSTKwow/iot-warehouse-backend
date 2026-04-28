package com.hstk.iot_warehouse.model.vo;

import com.hstk.iot_warehouse.model.entity.WmsIoRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsIoRecordVO extends WmsIoRecord {
    /**
     * 物资名称
     */
    private String materialName;
    /**
     * 物资编码
     */
    private String materialCode;
    /**
     * 库位ID
     */
    private String location;
    /**
     * 库位名称
     */
    private String locationName;
    /**
     * 区域名称
     */
    private String zoneName;
    /**
     * 对应RFID物资数量
     */
    private BigDecimal quantity;
}
