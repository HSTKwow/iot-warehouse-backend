package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.component.SensorRealtimeCache;
import com.hstk.iot_warehouse.mapper.IotSensorDataMapper;
import com.hstk.iot_warehouse.model.entity.IotSensorData;
import com.hstk.iot_warehouse.service.IotSensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IotSensorDataServiceImpl implements IotSensorDataService {

    @Autowired
    private IotSensorDataMapper iotSensorDataMapper;

    @Autowired
    private SensorRealtimeCache sensorRealtimeCache;

    @Override
    public IotSensorData getLatestByDeviceId(String deviceId) {
        // 先走实时内存缓存，低延迟
        IotSensorData realtime = sensorRealtimeCache.get(deviceId);
        if (realtime != null) {
            return realtime;
        }
        // 缓存没有再回退数据库
        return iotSensorDataMapper.selectLatestByDevice(deviceId);
    }

    @Override
    public List<IotSensorData> getHistory(String deviceId, int limit) {
        return iotSensorDataMapper.selectRecentByDevice(deviceId, limit);
    }

    @Override
    public List<IotSensorData> getHistoryByRange(String deviceId, LocalDateTime start, LocalDateTime end) {
        return iotSensorDataMapper.selectByDeviceIdAndTimeRange(deviceId, start, end);
    }
}
