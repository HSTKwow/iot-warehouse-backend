package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.WmsStockItem;
import com.hstk.iot_warehouse.model.vo.WmsStockItemVO;
import com.hstk.iot_warehouse.model.dto.StockSummaryDTO;
import com.hstk.iot_warehouse.common.api.PageResult;
import java.util.List;

public interface WmsStockItemService {

    /**
     * 获取所有库存明细（包含物资名称、编码等信息）
     * @return 详细库存列表
     */
    List<WmsStockItemVO> getAll();
    
    /**
     * 获取库存概览统计信息
     * @return 统计数据列表
     */
    List<StockSummaryDTO> getStockSummary();

    /**
     * 获取库存概览统计信息（分页）
     */
    PageResult<StockSummaryDTO> getStockSummary(int page, int pageSize);

    /**
     * 获取濒临过期库存（分页）
     */
    PageResult<WmsStockItem> getAboutToExpire(int page, int pageSize);

    /**
     * 根据ID获取库存项
     * @param itemId 库存项ID
     * @return 库存项
     */
    WmsStockItem getById(Long itemId);

    /**
     * 新增库存（入库）
     * @param item 库存项
     * @return 影响行数
     */
    int add(WmsStockItem item);

    /**
     * 更新库存（移库/状态变更）
     * @param item 库存项
     * @return 影响行数
     */
    int update(WmsStockItem item);

    /**
     * 删除库存
     * @param itemId 库存项ID
     * @return 影响行数
     */
    int deleteById(Long itemId);

    /**
     * 根据物资ID查询所有库存RFID明细
     * @param materialId 物资ID
     * @return 详细库存列表
     */
    List<WmsStockItemVO> getByMaterialId(Long materialId);

    /**
     * 根据RFID查询库存项
     * @param rfid RFID标签用于
     * @return 库存项
     */
    WmsStockItem getByRfid(String rfid);

    /**
     * 根据 RFID 更新库存状态（写卡成功回调）
     */
    int updateStatusByRfid(String rfidTag, Integer status);

    /**
     * 扫描所有物资库存水平，为库存不足的物资生成告警
     */
    void scanAllStockAlarms();

    /**
     * 检查单个物资的库存告警情况 (更新库存或更新阈值后调用)
     * @param materialId 物资ID
     */
    void checkAlarmForMaterial(Long materialId);
    /**
     * 查询指定物资最新一条待入库记录（status=0，写卡回调后创建）
     * @param materialId 物资ID
     * @return 最新待入库记录，没有则返回null
     */
    WmsStockItem getPendingByMaterialId(Long materialId);}
