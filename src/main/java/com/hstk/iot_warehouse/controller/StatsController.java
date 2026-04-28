package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.IotDevice;
import com.hstk.iot_warehouse.model.dto.IoTrendDTO;
import com.hstk.iot_warehouse.service.IotAlarmService;
import com.hstk.iot_warehouse.service.IotDeviceService;
import com.hstk.iot_warehouse.service.WmsIoRecordService;
import com.hstk.iot_warehouse.service.WmsMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private WmsIoRecordService wmsIoRecordService;

    @Autowired
    private WmsMaterialService wmsMaterialService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotAlarmService iotAlarmService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 今日出入库操作等
        stats.put("todayOps", wmsIoRecordService.countToday());

        // 设备在线/离线/总数
        List<IotDevice> allDevices = iotDeviceService.getAllDevices();
        long totalDevices  = allDevices.size();
        long onlineDevices = allDevices.stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1).count();
        long offlineDevices = totalDevices - onlineDevices;
        stats.put("totalDevices",   totalDevices);
        stats.put("onlineDevices",  onlineDevices);
        stats.put("offlineDevices", offlineDevices);

        // 未处理告警数
        stats.put("unprocessedAlarms", iotAlarmService.countUnprocessed());

        // 库存预警数（低于最小库存的物资种数）
        stats.put("lowStock", wmsMaterialService.countLowStock());

        return Result.success(stats);
    }

    @GetMapping("/io-trend")
    public Result<IoTrendDTO> getIoTrend(@RequestParam Long materialId,
                                         @RequestParam(defaultValue = "week") String range) {
        return Result.success(wmsIoRecordService.getIoTrend(materialId, range));
    }
}
