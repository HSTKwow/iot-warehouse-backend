package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.dto.IoTrendDTO;
import com.hstk.iot_warehouse.model.entity.WmsIoRecord;
import com.hstk.iot_warehouse.model.vo.WmsIoRecordVO;
import com.hstk.iot_warehouse.common.api.PageResult;
import java.util.List;

public interface WmsIoRecordService {
    /**
     * 添加出入库记录
     * @param rfid RFID标签号
     * @param type 类型 (1:入库, 2:出库)
     * @param orderNo 关联单号
     */
    void addRecord(String rfid, Integer type, String orderNo);

    /**
     * 添加出入库记录（带设备ID）
     * @param rfid      RFID标签号
     * @param type      类型 (1:入库, 2:出库)
     * @param orderNo   关联单号
     * @param deviceId  操作设备ID
     */
    void addRecord(String rfid, Integer type, String orderNo, String deviceId);

    /**
     * 获取所有历史记录（包含详情）
     * @return 记录列表VO
     */
    List<WmsIoRecordVO> getAll();

    /**
     * 获取所有历史记录（包含详情）分页
     */
    PageResult<WmsIoRecordVO> getAll(int page, int pageSize);

    /**
     * 统计今日操作数
     */
    long countToday();

    /**
     * 统计本周操作数
     */
    long countWeek();

    /**
     * 按物资统计指定时间范围的出入库趋势
     */
    IoTrendDTO getIoTrend(Long materialId, String range);
}
