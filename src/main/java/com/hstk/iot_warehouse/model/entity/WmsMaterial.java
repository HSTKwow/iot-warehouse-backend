package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsMaterial {
    /** 物资ID */
    private Long materialId;
    /** 物资编码 (条码/内部码) */
    private String materialCode;
    /** 物资名称 */
    private String name;
    /** 所属分类ID */
    private Long categoryId;
    /** 单位 (个, 箱, kg 等) */
    private String unit;
    /** 规格型号 */
    private String spec;
    /** 有效期预警天数 (过期前多少天预警) */
    private Integer expiryWarningDays;
    /** 最低库存预警值 */
    private Integer minStock;
    /** 最高库存积压值 */
    private Integer maxStock;
    /** 图片URL */
    private String imageUrl;
    /** 创建时间 */
    private java.time.LocalDateTime createTime;
    /** 更新时间 */
    private java.time.LocalDateTime updateTime;
}
