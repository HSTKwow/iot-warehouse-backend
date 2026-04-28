package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.IotSwitchLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 开关操作日志Mapper接口
 */
@Mapper
public interface IotSwitchLogMapper {
    /**
     * 插入操作日志
     * @param log 日志对象
     * @return 影响行数
     */
    int insert(IotSwitchLog log);

    /**
     * 更新日志
     * @param log 日志对象
     * @return 影响行数
     */
    int update(IotSwitchLog log);

    /**
     * 删除日志
     * @param logId 日志ID
     * @return 影响行数
     */
    int deleteById(Long logId);

    /**
     * 根据ID查询日志
     * @param logId 日志ID
     * @return 日志对象
     */
    IotSwitchLog selectById(Long logId);

    /**
     * 根据设备和组件查询
     * @param deviceId 设备ID
     * @param componentName 组件名称
     * @return 日志列表
     */
    List<IotSwitchLog> selectByDeviceAndComponent(String deviceId, String componentName);

    /**
     * 查询最近操作日志
     * @param limit 条数限制
     * @return 日志列表
     */
    List<IotSwitchLog> selectRecent(int limit);

    /**
     * 查询指定设备的操作日志
     * @param deviceId 设备ID
     * @param limit 条数限制
     * @return 日志列表
     */
    List<IotSwitchLog> selectByDeviceId(String deviceId, int limit);
}