package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.component.event.StockAlarmCheckEvent;
import com.hstk.iot_warehouse.mapper.WmsStockItemMapper;
import com.hstk.iot_warehouse.mapper.IotAlarmMapper;
import com.hstk.iot_warehouse.model.entity.IotAlarm;
import com.hstk.iot_warehouse.model.entity.WmsStockItem;
import com.hstk.iot_warehouse.model.vo.WmsStockItemVO;
import com.hstk.iot_warehouse.service.IotAlarmService;
import com.hstk.iot_warehouse.service.WmsStockItemService;
import com.hstk.iot_warehouse.service.WmsIoRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class WmsStockItemServiceImpl implements WmsStockItemService {

    @Autowired
    private WmsStockItemMapper wmsStockItemMapper;
    
    @Autowired
    private WmsIoRecordService wmsIoRecordService;

    @Autowired
    private IotAlarmService iotAlarmService;

    @Autowired
    private IotAlarmMapper iotAlarmMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<WmsStockItemVO> getAll() {
        return wmsStockItemMapper.selectAllWithMaterial();
    }

    @Override
    public List<com.hstk.iot_warehouse.model.dto.StockSummaryDTO> getStockSummary() {
        // Keep for backward compatibility or refactor to call paginated with default?
        // Ideally should support overload in Mapper too, but I removed non-arg method in interface?
        // Wait, I updated mapper interface in existing file, but I removed no-arg method "selectStockSummary()"?
        // Let me check my Step 2 replace.
        // I REPLACED "List... selectStockSummary();" WITH "List... selectStockSummary(limit, offset);"
        // So I broke "wmsStockItemMapper.selectStockSummary()" call here!
        // I must fix this method too, or pass a large limit.
        return wmsStockItemMapper.selectStockSummary(1000, 0); 
    }

    @Override
    public com.hstk.iot_warehouse.common.api.PageResult<com.hstk.iot_warehouse.model.dto.StockSummaryDTO> getStockSummary(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        java.util.List<com.hstk.iot_warehouse.model.dto.StockSummaryDTO> list = wmsStockItemMapper.selectStockSummary(pageSize, offset);
        long total = wmsStockItemMapper.countStockSummary();
        
        int lowStock = wmsStockItemMapper.countLowStock();
        int overStock = wmsStockItemMapper.countOverStock();
        java.math.BigDecimal totalQty = wmsStockItemMapper.sumTotalQuantity();
        
        com.hstk.iot_warehouse.common.api.PageResult<com.hstk.iot_warehouse.model.dto.StockSummaryDTO> result =
                new com.hstk.iot_warehouse.common.api.PageResult<>(total, list, lowStock, overStock, totalQty);
        return result;
    }

    @Override
    public com.hstk.iot_warehouse.common.api.PageResult<WmsStockItem> getAboutToExpire(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        java.util.List<WmsStockItem> list = wmsStockItemMapper.selectAboutToExpire(pageSize, offset);
        long total = wmsStockItemMapper.countAboutToExpire();
        return new com.hstk.iot_warehouse.common.api.PageResult<>(total, list);
    }

    @Override
    public WmsStockItem getById(Long itemId) {
        return wmsStockItemMapper.selectById(itemId);
    }

    @Override
    public int add(WmsStockItem item) {
        if (item.getInTime() == null) {
            item.setInTime(java.time.LocalDateTime.now());
        }
        int rows = wmsStockItemMapper.insert(item);
        if (rows > 0) {
            // 只有直接以 status=1（在库）方式添加时才记录入库流水
            // status=0（写卡预登记/待入库）不记录，由 MQTT inbound 扫描时记录
            if (item.getStatus() != null && item.getStatus() == 1) {
                wmsIoRecordService.addRecord(item.getRfidTag(), 1, "IN-" + System.currentTimeMillis());
            }
            // 检查库存水平，自动生成/恢复告警
            checkStockAlarm(item.getMaterialId());
        }
        return rows;
    }

    @Override
    public int update(WmsStockItem item) {
        int rows = wmsStockItemMapper.update(item);
        if (rows > 0 && item.getStatus() != null && item.getStatus() == 2) {
            // status=2 才是出库，由 MqttListener 负责记录，此处保留兜底（Web 手动操作场景）
            // 注意：MQTT 出库路径会单独调用 addRecord，避免重复；此处仅处理非 MQTT 的 Web 手动出库
        }
        if (rows > 0 && item.getMaterialId() != null) {
            // 出入库后检查库存水平
            checkStockAlarm(item.getMaterialId());
        }
        return rows;
    }

    @Override
    public int deleteById(Long itemId) {
        return wmsStockItemMapper.deleteById(itemId);
    }

    @Override
    public List<WmsStockItemVO> getByMaterialId(Long materialId) {
        return wmsStockItemMapper.selectByMaterialId(materialId);
    }

    @Override
    public WmsStockItem getByRfid(String rfid) {
        return wmsStockItemMapper.selectByRfid(rfid);
    }

    @Override
    public int updateStatusByRfid(String rfidTag, Integer status) {
        return wmsStockItemMapper.updateStatusByRfid(rfidTag, status);
    }

    @Override
    public void checkAlarmForMaterial(Long materialId) {
        checkStockAlarm(materialId);
    }

    /**
     * 检查指定物资的库存水平，低于 minStock 时自动生成库存不足告警
     */
    private void checkStockAlarm(Long materialId) {
        eventPublisher.publishEvent(new StockAlarmCheckEvent(materialId, "stock_change"));
    }

    @Override
    public void scanAllStockAlarms() {
        try {
            List<Map<String, Object>> lowStockList = wmsStockItemMapper.selectLowStockMaterials();
            List<Map<String, Object>> overStockList = wmsStockItemMapper.selectOverStockMaterials();
            if (lowStockList == null) {
                lowStockList = java.util.Collections.emptyList();
            }
            if (overStockList == null) {
                overStockList = java.util.Collections.emptyList();
            }

            Set<String> lowStockIds = new HashSet<>();
            Set<String> overStockIds = new HashSet<>();

            log.info("库存扫描: 低库存 {} 种, 积压 {} 种",
                    lowStockList == null ? 0 : lowStockList.size(),
                    overStockList == null ? 0 : overStockList.size());

            // 处理库存不足
            for (Map<String, Object> item : lowStockList) {
                Long materialId = toLong(item.get("materialId"));
                if (materialId == null) continue;
                String materialName = (String) item.get("materialName");
                BigDecimal currentStock = new BigDecimal(item.get("currentStock").toString());
                BigDecimal minStock = new BigDecimal(item.get("minStock").toString());
                String unit = item.get("unit") != null ? item.get("unit").toString() : "";
                String materialKey = String.valueOf(materialId);
                lowStockIds.add(materialKey);

                upsertStockAlarm(
                        materialKey,
                        "库存不足",
                        currentStock.compareTo(BigDecimal.ZERO) == 0 ? 3 : 2,
                        String.format("%s 报警时库存 %s%s, 低于最低阈值 %s%s",
                        materialName, currentStock.stripTrailingZeros().toPlainString(), unit,
                        minStock.stripTrailingZeros().toPlainString(), unit)
                );

                // 同一物资若存在“库存积压”未处理告警，条件已切换，自动恢复
                resolveStockAlarmByType(materialKey, "库存积压", "库存状态已从积压切换为低库存");
            }

            // 处理库存积压
            for (Map<String, Object> item : overStockList) {
                Long materialId = toLong(item.get("materialId"));
                if (materialId == null) continue;
                String materialName = (String) item.get("materialName");
                BigDecimal currentStock = new BigDecimal(item.get("currentStock").toString());
                BigDecimal maxStock = new BigDecimal(item.get("maxStock").toString());
                String unit = item.get("unit") != null ? item.get("unit").toString() : "";
                String materialKey = String.valueOf(materialId);
                overStockIds.add(materialKey);

                upsertStockAlarm(
                        materialKey,
                        "库存积压",
                        2,
                        String.format("%s 当前库存 %s%s, 超过最高阈值 %s%s",
                                materialName, currentStock.stripTrailingZeros().toPlainString(), unit,
                                maxStock.stripTrailingZeros().toPlainString(), unit)
                );

                // 同一物资若存在“库存不足”未处理告警，条件已切换，自动恢复
                resolveStockAlarmByType(materialKey, "库存不足", "库存状态已从低库存切换为积压");
            }

            // 自动恢复已不满足条件的告警
            List<IotAlarm> existing = iotAlarmService.listBySource("stock");
            for (IotAlarm alarm : existing) {
                if (alarm == null || alarm.getStatus() == null || alarm.getStatus() != 0) continue;
                String relatedId = alarm.getRelatedId();
                String alarmType = alarm.getAlarmType();
                if ("库存不足".equals(alarmType) && !lowStockIds.contains(relatedId)) {
                    iotAlarmService.resolveWithRemark(alarm.getAlarmId(), "库存水平已恢复到最低阈值以上", "系统定时任务");
                } else if ("库存积压".equals(alarmType) && !overStockIds.contains(relatedId)) {
                    iotAlarmService.resolveWithRemark(alarm.getAlarmId(), "库存水平已恢复到最高阈值以下", "系统定时任务");
                }
            }
        } catch (Exception e) {
            log.error("库存告警扫描失败", e);
        }
    }

    private void upsertStockAlarm(String materialId, String alarmType, int severity, String content) {
        List<IotAlarm> existing = iotAlarmMapper.selectBySourceAndRelatedId("stock", materialId);
        IotAlarm target = existing.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 0 && alarmType.equals(a.getAlarmType()))
                .findFirst()
                .orElse(null);
        if (target != null) {
            target.setSeverity(severity);
            target.setContent(content);
            iotAlarmMapper.update(target);
            return;
        }

        IotAlarm alarm = new IotAlarm();
        alarm.setAlarmType(alarmType);
        alarm.setSeverity(severity);
        alarm.setContent(content);
        alarm.setAlarmSource("stock");
        alarm.setRelatedId(materialId);
        alarm.setStatus(0);
        alarm.setCreateTime(LocalDateTime.now());
        iotAlarmService.insert(alarm);
    }

    private void resolveStockAlarmByType(String materialId, String alarmType, String remark) {
        List<IotAlarm> existing = iotAlarmMapper.selectBySourceAndRelatedId("stock", materialId);
        existing.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 0 && alarmType.equals(a.getAlarmType()))
                .forEach(a -> iotAlarmService.resolveWithRemark(a.getAlarmId(), remark, "系统定时任务"));
    }

    private Long toLong(Object idObj) {
        if (idObj == null) return null;
        if (idObj instanceof Integer) return ((Integer) idObj).longValue();
        if (idObj instanceof Long) return (Long) idObj;
        return Long.parseLong(idObj.toString());
    }

    @Override
    public WmsStockItem getPendingByMaterialId(Long materialId) {
        return wmsStockItemMapper.selectLatestPendingByMaterialId(materialId);
    }
}
