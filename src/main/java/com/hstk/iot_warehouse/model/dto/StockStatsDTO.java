package com.hstk.iot_warehouse.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 库存统计数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockStatsDTO {
    /** 物资种类总数 */
    private Integer totalMaterials;
    /** 库存预警种类数 */
    private Integer warningCount;
    /** 库存不足种类数 */
    private Integer lowStockCount;
    /** 库存积压种类数 */
    private Integer overStockCount;
}
