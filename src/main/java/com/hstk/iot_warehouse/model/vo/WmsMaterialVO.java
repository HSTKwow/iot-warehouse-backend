package com.hstk.iot_warehouse.model.vo;

import com.hstk.iot_warehouse.model.entity.WmsMaterial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsMaterialVO extends WmsMaterial {
    /** 当前库存 (展示字段，非数据库列) */
    private Integer currentStock;
    
    /** 分类名称 (展示字段) */
    private String categoryName;
}
