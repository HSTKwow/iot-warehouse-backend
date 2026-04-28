package com.hstk.iot_warehouse.component;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 记录设备最近一次上报时实际使用的 MQTT topic 设备标识，
 * 解决数据库 deviceId 与硬件 topic 写法不一致的问题。
 */
@Component
public class DeviceTopicCache {

    private final ConcurrentMap<String, String> topicDeviceIds = new ConcurrentHashMap<>();

    public void put(String deviceId, String topicDeviceId) {
        if (deviceId == null || topicDeviceId == null) {
            return;
        }
        topicDeviceIds.put(deviceId, topicDeviceId);
    }

    public String getTopicDeviceId(String deviceId) {
        if (deviceId == null) {
            return null;
        }
        return topicDeviceIds.get(deviceId);
    }
}
