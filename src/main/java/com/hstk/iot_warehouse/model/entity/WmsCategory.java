package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 物资分类实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsCategory {
    /** 分类ID */
    private Long categoryId;
    /** 分类名称 */
    private String categoryName;
    /** 描述 */
    private String description;
}
