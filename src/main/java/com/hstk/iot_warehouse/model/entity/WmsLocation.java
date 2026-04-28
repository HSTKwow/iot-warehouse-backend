package com.hstk.iot_warehouse.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 仓库库位实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsLocation {
    /** 库位ID (如 A-01) */
    private String id;
    /** 库位名称 (如 A-01 (货架1层)) */
    private String name;
    /** 所属区域ID (如 A) */
    private String parentId;
    /** 层级类型 (1:区域/仓库, 2:具体货架/位) */
    private Integer level;
    /** 排序号 */
    private Integer sortOrder;
}
