package com.hstk.iot_warehouse.component;

import com.hstk.iot_warehouse.component.event.StockAlarmCheckEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 库存告警扫描器
 * - 应用启动后自动扫描一次库存水平
 * - 每小时定时扫描，为库存不足的物资生成告警
 */
@Slf4j
@Component
public class StockAlarmScanner {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 应用启动完成后执行一次库存告警扫描
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Alarm scan");
        eventPublisher.publishEvent(new StockAlarmCheckEvent(null, "startup"));
    }

    /**
     * 每小时定时扫描库存水平
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 3600000)
    public void scheduledScan() {
        log.info("定时库存告警扫描...");
        eventPublisher.publishEvent(new StockAlarmCheckEvent(null, "scheduled"));
    }
}
