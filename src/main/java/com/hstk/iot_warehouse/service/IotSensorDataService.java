package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.IotSensorData;
import java.time.LocalDateTime;
import java.util.List;

public interface IotSensorDataService {
    IotSensorData getLatestByDeviceId(String deviceId);
    List<IotSensorData> getHistory(String deviceId, int limit);
    List<IotSensorData> getHistoryByRange(String deviceId, LocalDateTime start, LocalDateTime end);
}
