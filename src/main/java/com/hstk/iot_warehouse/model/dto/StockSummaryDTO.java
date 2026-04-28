package com.hstk.iot_warehouse.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 库存概览数据传输对象 (DTO)
 * 用于前端"库存统计"页面的列表展示，聚合了物资信息和库存总数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryDTO {
    /** 物资ID */
    private Long materialId;
    /** 物资名称 */
    private String materialName;
    /** 库存总数量 (聚合计算得出) */
    private BigDecimal quantity; 
    /** 最低库存预警值 */
    private Integer minStock;
    /** 最高库存积压值 */
    private Integer maxStock;
    /** 单位 */
    private String unit;
    /** 分类名称 */
    private String categoryName;
    /** 物资图片(OSS链接等) */
    private String imageUrl;
    /** 最近过期日期 (聚合计算得出，显示最早要过期的一批) */
    private java.time.LocalDate nearestExpiryDate; 
}
