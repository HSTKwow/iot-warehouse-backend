package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.IotSensorData;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 传感器数据Mapper接口
 */
@Mapper
public interface IotSensorDataMapper {
    /**
     * 插入传感器数据
     * @param data 数据对象
     * @return 影响行数
     */
    int insert(IotSensorData data);

    /**
     * 更新数据（一般较少使用）
     * @param data 数据对象
     * @return 影响行数
     */
    int update(IotSensorData data);

    /**
     * 删除数据
     * @param dataId 数据ID
     * @return 影响行数
     */
    int deleteById(Long dataId);

    /**
     * 根据ID查询数据
     * @param dataId 数据ID
     * @return 数据对象
     */
    IotSensorData selectById(Long dataId);

    /**
     * 查询设备所有数据
     * @param deviceId 设备ID
     * @return 数据列表
     */
    List<IotSensorData> selectByDeviceId(String deviceId);

    /**
     * 按时间范围查询设备数据
     * @param deviceId 设备ID
     * @param from 开始时间
     * @param to 结束时间
     * @return 数据列表
     */
    List<IotSensorData> selectByDeviceIdAndTimeRange(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * 查询设备最近N条数据
     * @param deviceId 设备ID
     * @param limit 条数限制
     * @return 数据列表
     */
    List<IotSensorData> selectRecentByDevice(String deviceId, int limit);

    /**
     * 查询设备最新一条数据
     * @param deviceId 设备ID
     * @return 数据对象
     */
    IotSensorData selectLatestByDevice(String deviceId);

    /**
     * 删除旧数据（清理）
     * @return 影响行数
     */
    int deleteOldData();
}