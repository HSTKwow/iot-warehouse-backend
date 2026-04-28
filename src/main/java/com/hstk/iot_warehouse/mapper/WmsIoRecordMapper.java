package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.dto.IoTrendRowDTO;
import com.hstk.iot_warehouse.model.entity.WmsIoRecord;
import com.hstk.iot_warehouse.model.vo.WmsIoRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出入库记录Mapper接口
 */
@Mapper
public interface WmsIoRecordMapper {
    /**
     * 获取所有记录（带详情）
     */
    List<WmsIoRecordVO> selectAllWithInfo(@org.apache.ibatis.annotations.Param("limit") int limit, @org.apache.ibatis.annotations.Param("offset") int offset);

    /**
     * 统计总数
     */
    long countAll();

    /**
     * 插入记录
     * @param record 记录对象
     * @return 影响行数
     */
    int insert(WmsIoRecord record);

    /**
     * 更新记录
     * @param record 记录对象
     * @return 影响行数
     */
    int update(WmsIoRecord record);

    /**
     * 删除记录
     * @param recordId 记录ID
     * @return 影响行数
     */
    int deleteById(Long recordId);

    /**
     * 根据ID查询
     * @param recordId 记录ID
     * @return 记录对象
     */
    WmsIoRecord selectById(Long recordId);

    /**
     * 根据RFID查询记录
     * @param rfidTag RFID标签
     * @param limit 条数限制
     * @return 记录列表
     */
    List<WmsIoRecord> selectByRfid(String rfidTag, int limit);

    /**
     * 按时间范围查询
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    List<WmsIoRecord> selectByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按类型查询 (入库/出库)
     * @param ioType 类型
     * @param limit 条数限制
     * @return 记录列表
     */
    List<WmsIoRecord> selectByIOType(Integer ioType, int limit);

    /**
     * 按订单号查询
     * @param orderNo 订单号
     * @return 记录列表
     */
    List<WmsIoRecord> selectByOrderNo(String orderNo);

    /**
     * 按扫描设备查询
     * @param deviceId 设备ID
     * @param limit 条数限制
     * @return 记录列表
     */
    List<WmsIoRecord> selectByDevice(String deviceId, int limit);

    /**
     * 查询最近记录
     * @param limit 条数限制
     * @return 记录列表
     */
    List<WmsIoRecord> selectRecent(int limit);

    /**
     * 根据物资ID查询最新的出入库记录
     */
    WmsIoRecord selectLatestByMaterialId(Long materialId);

    /**
     * 按物资和时间范围统计出入库趋势
     */
    List<IoTrendRowDTO> selectIoTrendByMaterial(@Param("materialId") Long materialId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("groupType") String groupType);
}
