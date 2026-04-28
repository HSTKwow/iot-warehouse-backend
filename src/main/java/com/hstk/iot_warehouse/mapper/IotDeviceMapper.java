package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.IotDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物联网设备Mapper接口
 */
@Mapper
public interface IotDeviceMapper {
    /**
     * 插入设备
     * @param device 设备对象
     * @return 影响行数
     */
    int insert(IotDevice device);

    /**
     * 更新设备
     * @param device 设备对象
     * @return 影响行数
     */
    int update(IotDevice device);

    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 影响行数
     */
    int deleteById(String deviceId);

    /**
     * 根据ID查询设备
     * @param deviceId 设备ID
     * @return 设备对象
     */
    IotDevice selectById(String deviceId);

    /**
     * 按归一化后的设备ID查询，兼容大小写和常见分隔符差异。
     */
    IotDevice selectByNormalizedId(@Param("normalizedDeviceId") String normalizedDeviceId);

    /**
     * 查询所有设备
     * @return 设备列表
     */
    List<IotDevice> selectAll();
    
    /**
     * 根据所有者ID查询设备
     * @param ownerId 所有者ID
     * @return 设备列表
     */
    List<IotDevice> selectByOwnerId(Long ownerId);

    /**
     * Check if user is assigned to device
     */
    int checkAssignment(@Param("deviceId") String deviceId, @Param("userId") Long userId);
    
    /**
     * Assign device to user
     */
    int assignDevice(@Param("deviceId") String deviceId, @Param("userId") Long userId);
    
    /**
     * Unassign device from user
     */
    int unassignDevice(@Param("deviceId") String deviceId, @Param("userId") Long userId);

    /**
     * Get user IDs assigned to device
     */
    List<Long> getAssignedUserIds(String deviceId);
}
