package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.component.RfidWriteCache;
import com.hstk.iot_warehouse.model.entity.WmsStockItem;
import com.hstk.iot_warehouse.service.WmsStockItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/stock")
public class WmsStockItemController {

    @Autowired
    private WmsStockItemService wmsStockItemService;

    @Autowired
    private RfidWriteCache rfidWriteCache;

    /**
     * 写卡前预登记：将本次写入的数量、库位、批次、有效期存入缓存
     * 前端在 POST prepare-write → MQTT write 的顺序调用，确保 MQTT 回调时能读到完整数据
     */
    @PostMapping("/prepare-write")
    public Result<String> prepareWrite(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        RfidWriteCache.Entry entry = new RfidWriteCache.Entry();
        if (body.get("quantity") != null) {
            entry.setQuantity(new BigDecimal(body.get("quantity").toString()));
        }
        if (body.get("location") != null) {
            entry.setLocation(body.get("location").toString());
        }
        if (body.get("batchNo") != null && !body.get("batchNo").toString().isEmpty()) {
            entry.setBatchNo(body.get("batchNo").toString());
        }
        if (body.get("expiryDate") != null && !body.get("expiryDate").toString().isEmpty()) {
            entry.setExpiryDate(LocalDate.parse(body.get("expiryDate").toString()));
        }
        rfidWriteCache.put(materialId, entry);
        log.info("[RFID] prepare-write cached: materialId={}, qty={}, location={}",
                materialId, entry.getQuantity(), entry.getLocation());
        return Result.success("ok");
    }

    /**
     * 获取所有库存明细（包含物资详情和位置信息）
     * @return 库存列表VO
     */
    @GetMapping
    public Result<List<com.hstk.iot_warehouse.model.vo.WmsStockItemVO>> getAll() {
        return Result.success(wmsStockItemService.getAll());
    }

    /**
     * 获取库存概览统计 (强制分页，默认第一页)
     */
    @GetMapping("/summary")
    public Result<?> getSummary(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("stock page: {}, pageSize: {}", page, pageSize);
        return Result.success(wmsStockItemService.getStockSummary(page, pageSize));
    }

    /**
     * 根据物资ID获取库存明细
     * @param id 物资ID
     * @return 该物资的库存列表
     */
    @GetMapping("/material/{id}")
    public Result<List<com.hstk.iot_warehouse.model.vo.WmsStockItemVO>> getByMaterialId(@PathVariable Long id) {
        return Result.success(wmsStockItemService.getByMaterialId(id));
    }

    /**
     * 查询指定物资最新待入库记录（写卡成功后前端轮询确认）
     * @param materialId 物资ID
     * @return 最新 status=0 的库存项，不存在则 null
     */
    @GetMapping("/pending/{materialId}")
    public Result<WmsStockItem> getPendingByMaterialId(@PathVariable Long materialId) {
        WmsStockItem item = wmsStockItemService.getPendingByMaterialId(materialId);
        return Result.success(item);
    }

    /**
     * 更新库存信息（移库、状态变更等）
     * @param item 更新的库存信息
     * @return 结果
     */
    @PutMapping
    public Result<String> update(@RequestBody WmsStockItem item) {
        wmsStockItemService.update(item);
        return Result.success("Stock item updated");
    }
}
