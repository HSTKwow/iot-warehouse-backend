package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.IotAlarm;
import com.hstk.iot_warehouse.service.IotAlarmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alarms")
public class IotAlarmController {

    @Autowired
    private IotAlarmService iotAlarmService;

    /**
     * 从请求中获取用户ID(admin返回null表示查全部)
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if ("admin".equals(role)) {
            return null;
        }
        Object idObj = request.getAttribute("id");
        if (idObj == null) {
            return null;
        }
        if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        }
        if (idObj instanceof Long) {
            return (Long) idObj;
        }
        // Fallback for string or other numbers
        try {
            return Long.valueOf(idObj.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse userId: {}", idObj);
            return null;
        }
    }

    /**
     * 获取所有告警列表(普通用户只看自己设备的告警)
     */
    @GetMapping
    public Result<List<IotAlarm>> listAll(
            HttpServletRequest request,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Integer status) {
        Long userId = getUserIdFromRequest(request);
        List<IotAlarm> list;
        if (deviceId != null && !deviceId.isEmpty()) {
            list = iotAlarmService.listByDeviceId(deviceId);
        } else if (source != null && !source.isEmpty()) {
            // source 和 status 可以同时过滤
            list = iotAlarmService.listBySourceAndStatus(source, status);
        } else if (status != null && status == 0) {
            list = iotAlarmService.listUnprocessed();
        } else {
            list = iotAlarmService.listAll(userId);
        }
        return Result.success(list);
    }

    /**
     * 获取告警统计(普通用户只统计自己设备的告警)
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Map<String, Object> stats = new HashMap<>();
        List<IotAlarm> all = iotAlarmService.listAll(userId);

        int total = all.size();
        // 从已过滤的列表中计算未处理数量
        List<IotAlarm> unprocessed = all.stream().filter(a -> a.getStatus() != null && a.getStatus() == 0).toList();
        int unprocessedCount = unprocessed.size();
        int processedCount = total - unprocessedCount;

        // 按级别统计未处理
        int critical = (int) unprocessed.stream().filter(a -> a.getSeverity() != null && a.getSeverity() == 3).count();
        int warning = (int) unprocessed.stream().filter(a -> a.getSeverity() != null && a.getSeverity() == 2).count();
        int info = (int) unprocessed.stream().filter(a -> a.getSeverity() != null && a.getSeverity() == 1).count();

        stats.put("total", total);
        stats.put("unprocessed", unprocessedCount);
        stats.put("processed", processedCount);
        stats.put("critical", critical);
        stats.put("warning", warning);
        stats.put("info", info);
        return Result.success(stats);
    }

    /**
     * 处理告警(标记为已处理, 需填写处理措施, 自动记录处理人)
     */
    @PutMapping("/{alarmId}/resolve")
    public Result<Void> resolve(@PathVariable Long alarmId, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String remark = body.get("remark");
        if (remark == null || remark.trim().isEmpty()) {
            return Result.error("请填写处理措施");
        }
        // 获取当前用户信息作为处理人
        Object usernameObj = request.getAttribute("username");
        String resolveUser = usernameObj != null ? usernameObj.toString() : "未知用户";
        
        iotAlarmService.resolveWithRemark(alarmId, remark.trim(), resolveUser);
        return Result.success(null);
    }

}
