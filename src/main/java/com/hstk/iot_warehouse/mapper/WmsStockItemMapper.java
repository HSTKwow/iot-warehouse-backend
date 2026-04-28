package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.WmsStockItem;
import com.hstk.iot_warehouse.model.vo.WmsStockItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 库存实物Mapper接口
 */
@Mapper
public interface WmsStockItemMapper {
    /**
     * 获取所有库存项并携带物资信息
     * @return 包含物资信息的列表
     */
    List<WmsStockItemVO> selectAllWithMaterial();

    /**
     * 插入库存项
     * @param item 库存对象
     * @return 影响行数
     */
    int insert(WmsStockItem item);

    /**
     * 更新库存项
     * @param item 库存对象
     * @return 影响行数
     */
    int update(WmsStockItem item);

    /**
     * 删除库存项
     * @param itemId 库存ID
     * @return 影响行数
     */
    int deleteById(Long itemId);

    /**
     * 根据ID查询
     * @param itemId 库存ID
     * @return 库存对象
     */
    WmsStockItem selectById(Long itemId);

    /**
     * 根据RFID查询 (一般唯一)
     * @param rfidTag RFID标签
     * @return 库存对象
     */
    WmsStockItem selectByRfid(String rfidTag);

    /**
     * 根据物资资料ID查询所有库存
     * @param materialId 物资ID
     * @return 库存列表
     */
    List<WmsStockItemVO> selectByMaterialId(Long materialId);

    /**
     * 查询在指定日期之前过期的库存
     * @param date 指定日期
     * @return 库存列表
     */
    List<WmsStockItem> selectExpiringBefore(LocalDate date);

    /**
     * 查询当前在库的所有物品
     * @return 库存列表
     */
    List<WmsStockItem> selectInStock();

    /**
     * 查询已过期的物品
     * @return 库存列表
     */
    List<WmsStockItem> selectExpired();

    /**
     * 查询临期物品 (定义见XML逻辑)
     * @return 库存列表
     */
    List<WmsStockItem> selectAboutToExpire(@org.apache.ibatis.annotations.Param("limit") int limit, @org.apache.ibatis.annotations.Param("offset") int offset);

    /**
     * 统计临期物品数量
     */
    long countAboutToExpire();

    /**
     * 根据库位查询
     * @param location 库位
     * @return 库存列表
     */
    List<WmsStockItem> selectByLocation(String location);

    /**
     * 查询库存概览
     * @return 概览列表
     */
    List<com.hstk.iot_warehouse.model.dto.StockSummaryDTO> selectStockSummary(@org.apache.ibatis.annotations.Param("limit") int limit, @org.apache.ibatis.annotations.Param("offset") int offset);

    /**
     * 统计概览数量
     */
    long countStockSummary();

    /**
     * 统计低于最小库存的物资数量
     * @return 数量
     */
    int countLowStock();

    /**
     * 统计超过最大库存的物资数量
     * @return 数量
     */
    int countOverStock();

    /**
     * 统计所有在库库存总量
     * @return 总数量
     */
    java.math.BigDecimal sumTotalQuantity();

    /**
     * 更新状态
     * @param itemId 库存ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(Long itemId, Integer status);

    /**
     * 根据RFID更新状态
     * @param rfidTag RFID标签
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatusByRfid(String rfidTag, Integer status);

    /**
     * 统计指定物资ID列表中有库存的记录数
     * @param materialIds 物资ID列表
     * @return 记录数
     */
    int countByMaterialIds(List<Long> materialIds);

    /**
     * 查询指定物资的当前库存量、最低库存阈值和物资名称
     * @param materialId 物资ID
     * @return Map{currentStock, minStock, materialName}
     */
    Map<String, Object> selectStockLevelByMaterialId(Long materialId);

    /**
     * 查询所有库存不足的物资(库存<minStock)
     * @return 列表{materialId, materialName, minStock, unit, currentStock}
     */
    List<Map<String, Object>> selectLowStockMaterials();

    /**
     * 查询所有库存积压的物资(库存>maxStock)
     * @return 列表{materialId, materialName, maxStock, unit, currentStock}
     */
    List<Map<String, Object>> selectOverStockMaterials();

    /**
     * 查询指定物资最新一条待入库记录（status=0）
     * @param materialId 物资ID
     * @return 最新的待入库记录，没有则返回null
     */
    WmsStockItem selectLatestPendingByMaterialId(Long materialId);
}
