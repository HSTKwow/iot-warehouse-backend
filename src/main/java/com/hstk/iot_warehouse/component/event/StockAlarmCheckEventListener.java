package com.hstk.iot_warehouse.component.event;

import com.hstk.iot_warehouse.service.WmsStockItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 统一消费库存告警检查事件
 */
@Slf4j
@Component
public class StockAlarmCheckEventListener {

    @Autowired
    private WmsStockItemService wmsStockItemService;

    @EventListener
    public void onStockAlarmCheck(StockAlarmCheckEvent event) {
        log.info("接收库存告警检查事件: trigger={}, materialId={}", event.getTrigger(), event.getMaterialId());
        // 当前规则统一扫描，保证不足/积压/自动恢复逻辑一致
        wmsStockItemService.scanAllStockAlarms();
    }
}

