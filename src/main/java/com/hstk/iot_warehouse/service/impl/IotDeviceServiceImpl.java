package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.common.utils.DeviceIdUtils;
import com.hstk.iot_warehouse.mapper.IotDeviceMapper;
import com.hstk.iot_warehouse.mapper.IotSensorDataMapper;
import com.hstk.iot_warehouse.model.entity.IotDevice;
import com.hstk.iot_warehouse.model.entity.IotSensorData;
import com.hstk.iot_warehouse.service.IotDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class IotDeviceServiceImpl implements IotDeviceService {

    @Autowired
    private IotDeviceMapper iotDeviceMapper;

    @Autowired
    private IotSensorDataMapper iotSensorDataMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addDevice(IotDevice device) {
        IotDevice existing = iotDeviceMapper.selectById(device.getDeviceId());
        if (existing != null) {
            throw new RuntimeException("设备ID " + device.getDeviceId() + " 已存在");
        }
        device.setCreateTime(LocalDateTime.now());
        if (device.getStatus() == null) {
            device.setStatus(0);
        }
        iotDeviceMapper.insert(device);
    }

    @Override
    public void updateDevice(IotDevice device) {
        int rows = iotDeviceMapper.update(device);
        log.info("Update rows: {}", rows);
        if (rows == 0) {
            throw new RuntimeException("修改失败：找不到ID为 " + device.getDeviceId() + " 的设备，或数据无变化");
        }
    }

    @Override
    public void deleteDevice(String deviceId) {
        iotDeviceMapper.deleteById(deviceId);
    }

    @Override
    public IotDevice getDeviceById(String deviceId) {
        IotDevice device = findByFlexibleDeviceId(deviceId);
        if (device != null) {
            fillSensorData(java.util.Collections.singletonList(device));
        }
        return device;
    }

    @Override
    public IotDevice findByFlexibleDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        String rawDeviceId = deviceId.trim();
        IotDevice device = iotDeviceMapper.selectById(rawDeviceId);
        if (device != null) {
            return device;
        }

        String normalizedDeviceId = DeviceIdUtils.normalize(rawDeviceId);
        if (normalizedDeviceId == null) {
            return null;
        }

        return iotDeviceMapper.selectByNormalizedId(normalizedDeviceId);
    }

    @Override
    public List<IotDevice> getAllDevices() {
        List<IotDevice> devices = iotDeviceMapper.selectAll();
        fillSensorData(devices);
        return devices;
    }
    
    // ... (other methods)

    private void fillSensorData(List<IotDevice> devices) {
        for (IotDevice device : devices) {
            try {
                // 直接使用原始 deviceId (带冒号)
                String originalId = device.getDeviceId();
                IotSensorData data = iotSensorDataMapper.selectLatestByDevice(originalId);

                if (data != null) {
                    if (data.getTemperature() != null) device.setTemperature(data.getTemperature().doubleValue());
                    if (data.getHumidity() != null) device.setHumidity(data.getHumidity().doubleValue());
                    
                    if (data.getExtraInfo() != null) {
                        try {
                            com.fasterxml.jackson.databind.JsonNode extra = objectMapper.readTree(data.getExtraInfo());
                            if (extra.has("led")) device.setLedStatus(extra.get("led").asBoolean());
                            if (extra.has("alarm")) device.setAlarmStatus(extra.get("alarm").asBoolean());
                        } catch (Exception e) {
                            log.warn("Failed to parse extraInfo for device {}", device.getDeviceId());
                        }
                    }
                }
            } catch (Exception e) {
                 log.error("Error fetching sensor data for device {}", device.getDeviceId(), e);
            }
        }
    }

    @Override
    public void updateDeviceStatus(String deviceId, Integer status) {
        IotDevice device = findByFlexibleDeviceId(deviceId);
        if (device != null) {
            device.setStatus(status);
            iotDeviceMapper.update(device);
        } else {
            log.warn("Device not found for status update: {}", deviceId);
        }
    }

    @Override
    public long countOffline() {
        // Assume all devices retrieved, then count status == 0
        List<IotDevice> all = iotDeviceMapper.selectAll();
        return all.stream()
                .filter(d -> d.getStatus() == null || d.getStatus() == 0)
                .count();
    }

    @Override
    public List<IotDevice> getDevicesByOwner(Long ownerId) {
        List<IotDevice> devices = iotDeviceMapper.selectByOwnerId(ownerId);
        fillSensorData(devices);
        return devices;
    }

    @Override
    public List<Long> getAssignedUserIds(String deviceId) {
        return iotDeviceMapper.getAssignedUserIds(deviceId);
    }

    @Override
    public void assignDevice(String deviceId, Long userId) {
        // Check if assignment exists
        int count = iotDeviceMapper.checkAssignment(deviceId, userId);
        if (count > 0) {
            log.info("用户 {} 已经拥有设备 {}", userId, deviceId);
            return; // Already exists
        }
        
        int rows = iotDeviceMapper.assignDevice(deviceId, userId);
        if (rows == 0) {
             throw new RuntimeException("分配失败");
        }
    }
    
    @Override 
    public void unassignDevice(String deviceId, Long userId) {
         iotDeviceMapper.unassignDevice(deviceId, userId);
    }
}
