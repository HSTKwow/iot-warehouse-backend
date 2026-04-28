package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.component.DeviceTopicCache;
import com.hstk.iot_warehouse.model.entity.IotDeviceConfig;
import com.hstk.iot_warehouse.service.IotDeviceConfigService;
import com.hstk.iot_warehouse.service.IotDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/device-configs")
public class IotDeviceConfigController {

    @Autowired
    private IotDeviceConfigService iotDeviceConfigService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IMqttClient mqttClient;

    @Autowired
    private MqttConnectOptions mqttConnectOptions;

    @Autowired
    private DeviceTopicCache deviceTopicCache;

    @Autowired
    private jakarta.servlet.http.HttpServletRequest request;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{deviceId}")
    public Result<IotDeviceConfig> getConfig(@PathVariable String deviceId) {
        // 权限校验：只能查看自己的设备配置
        if (!hasDeviceAccess(deviceId)) {
            return Result.error("无权访问该设备配置");
        }
        IotDeviceConfig config = iotDeviceConfigService.getByDeviceId(deviceId);
        return Result.success(config);
    }

    @PostMapping
    public Result<IotDeviceConfig> updateConfig(@RequestBody IotDeviceConfig config) {
        // 权限校验：只能配置自己的设备
        if (!hasDeviceAccess(config.getDeviceId())) {
            return Result.error("无权配置该设备");
        }

        log.info("Updating device config: {}", config);
        IotDeviceConfig updated = iotDeviceConfigService.updateConfig(config);

        // 保存成功后，通过 MQTT 下发配置到硬件
        pushConfigToDevice(config.getDeviceId(), updated);

        return Result.success(updated);
    }

    /**
     * 权限校验：Admin 可以访问所有设备，普通用户只能访问分配给自己的设备
     */
    private boolean hasDeviceAccess(String deviceId) {
        Object roleObj = request.getAttribute("role");
        Object userIdObj = request.getAttribute("id");
        String role = (roleObj != null) ? roleObj.toString() : "user";

        if ("admin".equalsIgnoreCase(role)) {
            return true; // Admin 可以访问所有
        }

        if (userIdObj == null) return false;
        Long userId = Long.valueOf(userIdObj.toString());

        // 检查该设备是否分配给了该用户
        List<Long> assignedUsers = iotDeviceService.getAssignedUserIds(deviceId);
        return assignedUsers != null && assignedUsers.contains(userId);
    }

    /**
     * 通过 MQTT 将配置下发到硬件设备
     * Topic: device/{topicDeviceId}/cmd
     * Payload: {"config": {"tempMax": 30, "tempMin": 0, "humidityMax": 80, "smokeThreshold": 2000, ...}}
     */
    private void pushConfigToDevice(String deviceId, IotDeviceConfig config) {
        // 收到前端发来的命令时，通过日志输出
        String mqttDeviceId = resolveMqttDeviceId(deviceId);
        log.info("向目标设备 ID: {}, MQTT目标: {}, 下发阈值配置: {}", deviceId, mqttDeviceId, config);

        try {
            ensureMqttConnected();
            if (!mqttClient.isConnected()) {
                log.warn("MQTT not connected, skipping config push for {}", deviceId);
                return;
            }

            String topic = "device/" + mqttDeviceId + "/cmd";

            // 构建配置 payload
            Map<String, Object> configMap = new HashMap<>();
            if (config.getTempMax() != null) configMap.put("tempMax", config.getTempMax());
            if (config.getTempMin() != null) configMap.put("tempMin", config.getTempMin());
            if (config.getHumidityMax() != null) configMap.put("humidityMax", config.getHumidityMax());
            if (config.getSmokeThreshold() != null) configMap.put("smokeThreshold", config.getSmokeThreshold());
            if (config.getPressureMax() != null) configMap.put("pressureMax", config.getPressureMax());
            if (config.getDewPointMax() != null) configMap.put("dewPointMax", config.getDewPointMax());
            if (config.getLuxMax() != null) configMap.put("luxMax", config.getLuxMax());
            if (config.getWorkMode() != null) configMap.put("workMode", config.getWorkMode());
            if (config.getAlarmMode() != null) configMap.put("alarmMode", config.getAlarmMode());

            Map<String, Object> payload = new HashMap<>();
            payload.put("config", configMap);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            MqttMessage message = new MqttMessage(jsonPayload.getBytes());
            message.setQos(1);
            message.setRetained(false);
            mqttClient.publish(topic, message);

            log.info("Config pushed to device {} via MQTT: {}", deviceId, jsonPayload);
        } catch (Exception e) {
            log.error("Failed to push config to device {}", deviceId, e);
        }
    }

    private String resolveMqttDeviceId(String deviceId) {
        String cachedTopicDeviceId = deviceTopicCache.getTopicDeviceId(deviceId);
        return (cachedTopicDeviceId == null || cachedTopicDeviceId.isBlank()) ? deviceId : cachedTopicDeviceId;
    }

    private void ensureMqttConnected() {
        if (mqttClient.isConnected()) return;
        try {
            mqttClient.connect(mqttConnectOptions);
            log.info("下发配置前 MQTT 重连成功");
        } catch (MqttException e) {
            log.warn("下发配置前 MQTT 重连失败: {}", e.getMessage());
        }
    }
}
