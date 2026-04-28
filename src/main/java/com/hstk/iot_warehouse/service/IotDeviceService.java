package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.IotDevice;
import java.util.List;

public interface IotDeviceService {
    void addDevice(IotDevice device);
    void updateDevice(IotDevice device);
    void deleteDevice(String deviceId);
    IotDevice getDeviceById(String deviceId);
    IotDevice findByFlexibleDeviceId(String deviceId);
    List<IotDevice> getAllDevices();
    void updateDeviceStatus(String deviceId, Integer status);
    long countOffline();
    
    // New methods for device assignment
    List<IotDevice> getDevicesByOwner(Long ownerId);
    void assignDevice(String deviceId, Long userId);
    void unassignDevice(String deviceId, Long userId);
    List<Long> getAssignedUserIds(String deviceId);
}
