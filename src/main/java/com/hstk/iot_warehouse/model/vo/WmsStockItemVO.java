package com.hstk.iot_warehouse.model.vo;

import com.hstk.iot_warehouse.model.entity.WmsStockItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WmsStockItemVO extends WmsStockItem {
    /**
     * 物资名称
     */
    private String materialName;
    /**
     * 物资编码
     */
    private String materialCode;
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 规格型号
     */
    private String spec;
    /**
     * 单位
     */
    private String unit;
    /**
     * 库位名称（具体货架）
     */
    private String locationName;
    /**
     * 区域名称（所属区域）
     */
    private String zoneName;
}
