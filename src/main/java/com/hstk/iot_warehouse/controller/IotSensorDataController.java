package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.IotSensorData;
import com.hstk.iot_warehouse.service.IotSensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sensor-data")
@CrossOrigin
public class IotSensorDataController {

    @Autowired
    private IotSensorDataService iotSensorDataService;

    @GetMapping("/latest")
    public Result<IotSensorData> getLatest(@RequestParam String deviceId) {
        IotSensorData data = iotSensorDataService.getLatestByDeviceId(deviceId);
        return Result.success(data);
    }

    @GetMapping("/history")
    public Result<List<IotSensorData>> getHistory(@RequestParam String deviceId, @RequestParam(defaultValue = "10") int limit) {
        List<IotSensorData> list = iotSensorDataService.getHistory(deviceId, limit);
        return Result.success(list);
    }

    @GetMapping("/range")
    public Result<List<IotSensorData>> getHistoryByRange(
            @RequestParam String deviceId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        
        List<IotSensorData> list = iotSensorDataService.getHistoryByRange(deviceId, start, end);
        return Result.success(list);
    }
}
