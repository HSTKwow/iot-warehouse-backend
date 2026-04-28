package com.hstk.iot_warehouse.component;

import com.hstk.iot_warehouse.model.entity.IotSensorData;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 设备传感器实时缓存（内存态）
 * - 用于 latest 接口低延迟返回，避免等待数据库落库
 */
@Component
public class SensorRealtimeCache {

    private final ConcurrentMap<String, IotSensorData> latestByDevice = new ConcurrentHashMap<>();

    public void put(String deviceId, IotSensorData data) {
        if (deviceId == null || data == null) return;
        latestByDevice.put(deviceId, data);
    }

    public IotSensorData get(String deviceId) {
        if (deviceId == null) return null;
        return latestByDevice.get(deviceId);
    }
}

