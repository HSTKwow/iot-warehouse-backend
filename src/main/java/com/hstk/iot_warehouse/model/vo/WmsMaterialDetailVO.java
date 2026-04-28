package com.hstk.iot_warehouse.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 物资详情VO
 * 包含基础信息 + 库存实物明细列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsMaterialDetailVO extends WmsMaterialVO {
    /** 库存明细列表 */
    private List<WmsStockItemVO> stockItems;
}
