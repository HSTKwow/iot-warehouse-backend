package com.hstk.iot_warehouse.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> records;
    
    // Extra statistics
    private Integer lowStockCount;
    private Integer overStockCount;
    private java.math.BigDecimal totalQuantity;

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
