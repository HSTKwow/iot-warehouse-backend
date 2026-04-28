package com.hstk.iot_warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hstk.iot_warehouse.component.DeviceTopicCache;
import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.IotDevice;
import com.hstk.iot_warehouse.service.IotDeviceService;
import com.hstk.iot_warehouse.service.IotSwitchLogService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/devices")
@CrossOrigin // Allow cross-origin
public class IotDeviceController {

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotSwitchLogService iotSwitchLogService;

    @Autowired
    private IMqttClient mqttClient;

    @Autowired
    private MqttConnectOptions mqttConnectOptions;

    @Autowired
    private DeviceTopicCache deviceTopicCache;

    @Autowired
    private jakarta.servlet.http.HttpServletRequest request;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public Result<List<IotDevice>> getAll() {
        // Get user info from request attributes (set by Interceptor)
        Object roleObj = request.getAttribute("role");
        Object userIdObj = request.getAttribute("id");
        
        String role = (roleObj != null) ? roleObj.toString() : "user";
        Long userId = (userIdObj != null) ? Long.valueOf(userIdObj.toString()) : null;

        log.info("获取全部设备, User Role: {}, User ID: {}", role, userId);

        if ("admin".equalsIgnoreCase(role)) {
             // Admin sees all devices
             return Result.success(iotDeviceService.getAllDevices());
        } else {
             // Regular user sees only assigned devices
             if (userId != null) {
                 return Result.success(iotDeviceService.getDevicesByOwner(userId));
             } else {
                 return Result.success(List.of());
             }
        }
    }
    
    /**
     * Assign device to user (Admin only)
     * POST /devices/assign
     * Body: { "deviceId": "...", "userId": 123 }
     */
    @PostMapping("/assign")
    public Result<String> assignDevice(@RequestBody Map<String, Object> params) {
        Object roleObj = request.getAttribute("role");
        String role = (roleObj != null) ? roleObj.toString() : "user";
        
        if (!"admin".equalsIgnoreCase(role)) {
            return Result.error("只有管理员可以分配设备");
        }

        String deviceId = (String) params.get("deviceId");
        
        // Handle single or multiple users
        Object userIdObj = params.get("userId");
        if (userIdObj == null) {
            return Result.error("请选择用户");
        }

        if (userIdObj instanceof List) {
            // Bulk assignment
            List<?> userIds = (List<?>) userIdObj;
            for (Object uid : userIds) {
                 if (uid instanceof Number) {
                     iotDeviceService.assignDevice(deviceId, ((Number) uid).longValue());
                 }
            }
        } else if (userIdObj instanceof Number) {
            iotDeviceService.assignDevice(deviceId, ((Number) userIdObj).longValue());
        }

        return Result.success("设备分配成功");
    }
    
    /**
     * Get users assigned to a device (Admin only)
     */
    @GetMapping("/{id}/users")
    public Result<List<Long>> getAssignedUsers(@PathVariable String id) {
         Object roleObj = request.getAttribute("role");
         if (!"admin".equalsIgnoreCase(roleObj != null ? roleObj.toString() : "")) {
             return Result.error("无权操作");
         }
         return Result.success(iotDeviceService.getAssignedUserIds(id));
    }
    
    /**
     * Revoke access (Admin only)
     */
    @PostMapping("/revoke")
    public Result<String> revokeAccess(@RequestBody Map<String, Object> params) {
        // Similar check...
        String deviceId = (String) params.get("deviceId");
        Number userId = (Number) params.get("userId");
        
        if (deviceId != null && userId != null) {
            iotDeviceService.unassignDevice(deviceId, userId.longValue());
            return Result.success("已取消分配");
        }
        return Result.error("参数错误");
    }

    @GetMapping("/{id}")
    public Result<IotDevice> getById(@PathVariable String id) {
        log.info("获取设备");
        return Result.success(iotDeviceService.getDeviceById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody IotDevice device) {
        log.info("添加设备");
        iotDeviceService.addDevice(device);
        return Result.success("Added successfully");
    }

    @PutMapping
    public Result<String> update(@RequestBody IotDevice device) {
        log.info("更新设备信息");
        iotDeviceService.updateDevice(device);
        return Result.success("Updated successfully");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        iotDeviceService.deleteDevice(id);
        return Result.success("Deleted successfully");
    }

    /**
     * 发送控制指令到设备
     * POST /devices/{deviceId}/cmd
     * Body: {"led": true, "alarm": false}
     */
    @PostMapping("/{deviceId}/cmd")
    public Result<String> sendCommand(@PathVariable String deviceId, @RequestBody Map<String, Object> command) throws Exception {
        // 收到前端发来的命令时，通过 log 输出
        String mqttDeviceId = resolveMqttDeviceId(deviceId);
        log.info("向目标设备 ID: {}, MQTT目标: {}, 发送控制指令内容: {}", deviceId, mqttDeviceId, command);
        ensureMqttConnected();
        if (!mqttClient.isConnected()) return Result.error("MQTT Client not connected");
            
            // Topic: device/{topicDeviceId}/cmd
            String topic = "device/" + mqttDeviceId + "/cmd";
            
            String payload = objectMapper.writeValueAsString(command);
            
            log.info("[MQTT] Topic: {}, Payload: {}", topic, payload); // 添加详细日志

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // At least once delivery
            message.setRetained(false); // Do not retain command
            
            // 1. Send MQTT Command FIRST (Minimize latency)
            try {
                mqttClient.publish(topic, message);
            } catch (MqttException e) {
                log.error("MQTT publish failed, deviceId={}, mqttDeviceId={}, topic={}", deviceId, mqttDeviceId, topic, e);
                return Result.error("设备离线或MQTT连接异常，请稍后重试");
            }
            
            // 2. Log asynchronously (Don't block response)
            // Get User ID from request
            Object userIdObj = request.getAttribute("id");
            Long userId = (userIdObj != null) ? Long.valueOf(userIdObj.toString()) : null;

            java.util.concurrent.CompletableFuture.runAsync(() -> {
                command.forEach((k, v) -> {
                    if ("led".equals(k) || "alarm".equals(k)) {
                        String action = "UNKNOWN";
                        if (v instanceof Boolean) {
                            action = ((Boolean) v) ? "ON" : "OFF";
                        }
                        iotSwitchLogService.log(deviceId, k, action, "web", userId);
                    }
                });
            }).exceptionally(ex -> {
                log.error("Async logging failed", ex);
                return null;
            });
            
            return Result.success("Command sent: " + payload);
    }

    private String resolveMqttDeviceId(String deviceId) {
        String cachedTopicDeviceId = deviceTopicCache.getTopicDeviceId(deviceId);
        return (cachedTopicDeviceId == null || cachedTopicDeviceId.isBlank()) ? deviceId : cachedTopicDeviceId;
    }

    private void ensureMqttConnected() {
        if (mqttClient.isConnected()) return;
        try {
            mqttClient.connect(mqttConnectOptions);
            log.info("发送命令前 MQTT 重连成功");
        } catch (MqttException e) {
            log.warn("发送命令前 MQTT 重连失败: {}", e.getMessage());
        }
    }
}
