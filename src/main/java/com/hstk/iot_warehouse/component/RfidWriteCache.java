package com.hstk.iot_warehouse.component;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 写卡操作预登记缓存
 * <p>前端在发出 MQTT 写卡指令前，先 POST /stock/prepare-write 将本次写卡的
 * 数量、库位、批次号、有效期缓存到此处。
 * MQTT 回调收到 source=write 时，从缓存拉取这些字段填入 WmsStockItem。</p>
 */
@Component
public class RfidWriteCache {

    @Data
    public static class Entry {
        private BigDecimal quantity;
        private String location;
        private String batchNo;
        private LocalDate expiryDate;
    }

    /** key = materialId */
    private final ConcurrentHashMap<Long, Entry> cache = new ConcurrentHashMap<>();

    public void put(Long materialId, Entry entry) {
        cache.put(materialId, entry);
    }

    /** 取出并移除（一次性） */
    public Entry pop(Long materialId) {
        return cache.remove(materialId);
    }

    public Entry peek(Long materialId) {
        return cache.get(materialId);
    }
}
