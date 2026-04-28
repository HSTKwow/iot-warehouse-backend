package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.IotDeviceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备配置Mapper接口
 */
@Mapper
public interface IotDeviceConfigMapper {
    /**
     * 插入配置
     * @param config 配置对象
     * @return 插入后的对象
     */
    IotDeviceConfig insert(IotDeviceConfig config);

    /**
     * 更新配置
     * @param config 配置对象
     * @return 影响行数
     */
    int update(IotDeviceConfig config);

    /**
     * 删除配置
     * @param configId 配置ID
     * @return 影响行数
     */
    int deleteById(Long configId);

    /**
     * 根据ID查询
     * @param configId 配置ID
     * @return 配置对象
     */
    IotDeviceConfig selectById(Long configId);

    /**
     * 根据设备ID查询配置
     * @param deviceId 设备ID
     * @return 配置对象
     */
    IotDeviceConfig selectByDeviceId(String deviceId);

    /**
     * 查询所有配置
     * @return 配置列表
     */
    List<IotDeviceConfig> selectAll();
}