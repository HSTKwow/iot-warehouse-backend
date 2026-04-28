package com.hstk.iot_warehouse.component.event;

/**
 * 库存告警检查事件
 * materialId 为 null 表示全量扫描；非 null 表示由某个物资变更触发（当前实现仍执行统一扫描）。
 */
public class StockAlarmCheckEvent {

    private final Long materialId;
    private final String trigger;

    public StockAlarmCheckEvent(Long materialId, String trigger) {
        this.materialId = materialId;
        this.trigger = trigger;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public String getTrigger() {
        return trigger;
    }

    public boolean isFullScan() {
        return materialId == null;
    }
}

