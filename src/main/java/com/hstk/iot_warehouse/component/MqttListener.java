package com.hstk.iot_warehouse.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hstk.iot_warehouse.mapper.IotSensorDataMapper;
import com.hstk.iot_warehouse.mapper.WmsMaterialMapper;
import com.hstk.iot_warehouse.model.entity.IotAlarm;
import com.hstk.iot_warehouse.model.entity.IotDevice;
import com.hstk.iot_warehouse.model.entity.IotSensorData;
import com.hstk.iot_warehouse.model.entity.IotDeviceConfig;
import com.hstk.iot_warehouse.service.*;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MqttListener implements CommandLineRunner {

    @Autowired
    private IMqttClient mqttClient;

    @Autowired
    private MqttConnectOptions mqttConnectOptions;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IotSwitchLogService iotSwitchLogService;

    @Autowired
    private IotSensorDataMapper iotSensorDataMapper;

    @Autowired
    private IotAlarmService iotAlarmService;

    @Autowired
    private IotDeviceConfigService iotDeviceConfigService;

    @Autowired
    private WmsStockItemService wmsStockItemService;

    @Autowired
    private WmsIoRecordService wmsIoRecordService;

    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Autowired
    private RfidWriteCache rfidWriteCache;

    @Autowired
    private SensorRealtimeCache sensorRealtimeCache;

    @Autowired
    private DeviceTopicCache deviceTopicCache;

    @Value("${mqtt.topic.data}")
    private String topicData; // device/+/data

    @Value("${sensor.persist-min-interval-ms:2000}")
    private long persistMinIntervalMs; // 数据变化不明显时的最小入库间隔

    private final ObjectMapper objectMapper = new ObjectMapper();

    //缓存上一次保存的数据，用于去重和限流
    private final Map<String, IotSensorData> lastSavedData = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) throws Exception {
        // 设置回调处理重连逻辑
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.info("MQTT已连接 (重连={}). 订阅主题: {}", reconnect, topicData);
                try {
                    subscribeToTopic();
                } catch (Exception e) {
                    log.error("连接后订阅主题失败", e);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT连接丢失: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // 实际处理由订阅时指定的监听器完成
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 消息发送完成回调，当前项目不需要处理
            }
        });

        // 如果 MqttConfig 中初始连接失败，这里尝试重连
        if (!mqttClient.isConnected()) {
            log.info("MQTT 未连接，尝试连接...");
            try {
                mqttClient.connect(mqttConnectOptions);
                log.info("MQTT 重连成功");
                subscribeToTopic();
            } catch (MqttException e) {
                log.warn("MQTT 连接失败，将由 AutomaticReconnect 自动重试: {}", e.getMessage());
            }
        } else {
            subscribeToTopic();
        }
    }

    private void subscribeToTopic() throws Exception {
        IMqttMessageListener listener = new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                processMessage(topic, message);
            }
        };
        log.info("订阅MQTT主题: {}", topicData);
        mqttClient.subscribe(topicData, 0, listener);
    }

    private void processMessage(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                // 使用 debug 级别，只有在调试时才显示接收到的原始消息，避免刷屏
                log.debug("MQTT收到 [{}]: {}", topic, payload);

                // 检查主题格式: device/{deviceId}/data
                String[] parts = topic.split("/");
                if (parts.length < 3) return;

                String topicDeviceId = parts[1];
                IotDevice matchedDevice = iotDeviceService.findByFlexibleDeviceId(topicDeviceId);
                if (matchedDevice == null) {
                    log.debug("忽略未注册设备消息: topic={}, topicDeviceId={}", topic, topicDeviceId);
                    return;
                }

                String deviceId = matchedDevice.getDeviceId();
                deviceTopicCache.put(deviceId, topicDeviceId);

                try {
                    JsonNode json = objectMapper.readTree(payload);
                    
                    // 1. 检查在线状态
                    if (json.has("status")) {
                        String status = json.get("status").asText();
                        if ("online".equals(status)) {
                            iotDeviceService.updateDeviceStatus(deviceId, 1); // 1 = 在线
                            log.info("设备上线: {} (topicDeviceId={})", deviceId, topicDeviceId);
                        } else if ("offline".equals(status)) {
                            iotDeviceService.updateDeviceStatus(deviceId, 0); // 0 = 离线
                            log.info("设备离线: {} (topicDeviceId={})", deviceId, topicDeviceId);
                        }
                        return;
                    }

                    // 2. 除显式 status 外，只要收到该设备任意业务消息，也视为在线
                    // 避免出现“设备有数据但状态仍离线”（例如后端重启后只收到传感器数据未收到 status 包）
                    iotDeviceService.updateDeviceStatus(deviceId, 1);

                    // 检查RFID数据（单独处理）
                    if (json.has("cardId") || json.has("rawData")) {
                        String cardId = json.has("cardId") ? json.get("cardId").asText() : "unknown";
                        String materialCode = json.has("rawData") ? json.get("rawData").asText() : "";
                        String source = json.has("source") ? json.get("source").asText() : "read";
                        log.info("[RFID] 设备: {}, UID: {}, materialCode: {}, source: {}", deviceId, cardId, materialCode, source);

                        // 读卡模式：执行入库或出库
                        if (("inbound".equals(source) || "outbound".equals(source)) && !"unknown".equals(cardId)) {
                            final String finalCardId = cardId;
                            final String finalSource = source;
                            final String finalDeviceId = deviceId;
                            CompletableFuture.runAsync(() -> {
                                try {
                                    com.hstk.iot_warehouse.model.entity.WmsStockItem item =
                                            wmsStockItemService.getByRfid(finalCardId);
                                    if (item == null) {
                                        log.warn("[RFID] 未找到 rfidTag={} 对应的库存记录", finalCardId);
                                        return;
                                    }
                                    if ("inbound".equals(finalSource)) {
                                        if (item.getStatus() != 0) {
                                            log.warn("[RFID] 入库失败：rfidTag={} 当前状态={} 非待入库", finalCardId, item.getStatus());
                                            return;
                                        }
                                        // 库位优先使用预登记的库位；若未预登记才回落到设备位置
                                        if (item.getLocation() == null || item.getLocation().isEmpty()) {
                                            IotDevice device = iotDeviceService.getDeviceById(finalDeviceId);
                                            if (device != null && device.getLocation() != null) {
                                                item.setLocation(device.getLocation());
                                            }
                                        }
                                        item.setStatus(1);
                                        item.setInTime(LocalDateTime.now());
                                        wmsStockItemService.update(item);
                                        wmsIoRecordService.addRecord(finalCardId, 1,
                                                "IN-" + System.currentTimeMillis(), finalDeviceId);
                                        log.info("[RFID] 入库成功 rfidTag={} materialId={} location={}",
                                                finalCardId, item.getMaterialId(), item.getLocation());
                                    } else {
                                        if (item.getStatus() != 1) {
                                            log.warn("[RFID] 出库失败：rfidTag={} 当前状态={} 非在库", finalCardId, item.getStatus());
                                            return;
                                        }
                                        item.setStatus(2);
                                        wmsStockItemService.update(item);
                                        wmsIoRecordService.addRecord(finalCardId, 2,
                                                "OUT-" + System.currentTimeMillis(), finalDeviceId);
                                        log.info("[RFID] 出库成功 rfidTag={}", finalCardId);
                                    }
                                } catch (Exception e) {
                                    log.error("[RFID] 出入库处理失败 cardId={}", finalCardId, e);
                                }
                            });
                            return;
                        }

                        // 写卡来源才创建/更新待入库记录
                        if ("write".equals(source)
                                && materialCode != null && !materialCode.isEmpty() && !"null".equals(materialCode)
                                && !"unknown".equals(cardId)) {
                            CompletableFuture.runAsync(() -> {
                                try {
                                    com.hstk.iot_warehouse.model.entity.WmsMaterial material =
                                            wmsMaterialMapper.selectByCode(materialCode);
                                    if (material == null) {
                                        log.warn("[RFID] 未找到物资编码: {}", materialCode);
                                        return;
                                    }
                                    //同一张卡已有记录则更新（支持重复写卡重新绑定物资）
                                    com.hstk.iot_warehouse.model.entity.WmsStockItem existing =
                                            wmsStockItemService.getByRfid(cardId);
                                    // 从内存缓存取出预登记数据（一次性消费）
                                    RfidWriteCache.Entry cached = rfidWriteCache.pop(material.getMaterialId());
                                    if (existing != null) {
                                        // 卡片重写：删除旧记录，避免残留旧的数量/批次/库位/有效期
                                        wmsStockItemService.deleteById(existing.getItemId());
                                        log.info("[RFID] 卡片重写，已清除旧记录 itemId={}, rfidTag={}",
                                                existing.getItemId(), cardId);
                                    }
                                    // 全新插入（无论新卡还是重写）
                                    com.hstk.iot_warehouse.model.entity.WmsStockItem newItem =
                                            new com.hstk.iot_warehouse.model.entity.WmsStockItem();
                                    newItem.setRfidTag(cardId);
                                    newItem.setMaterialId(material.getMaterialId());
                                    newItem.setStatus(0); // 0 = 待入库
                                    newItem.setInTime(LocalDateTime.now());
                                    applyCacheToItem(newItem, cached);
                                    wmsStockItemService.add(newItem);
                                    log.info("[RFID] 写卡成功，已创建库存记录 rfidTag={}, materialId={}, qty={}, loc={}",
                                            cardId, material.getMaterialId(),
                                            newItem.getQuantity(), newItem.getLocation());
                                } catch (Exception e) {
                                    log.error("[RFID] 创建库存记录失败 cardId={}", cardId, e);
                                }
                            });
                        }

                        return;
                    }

                    // 报警事件处理
                    if (json.has("alarm_event") && json.get("alarm_event").asBoolean()) {
                        String alarmType = json.has("alarmType") ? json.get("alarmType").asText() : "环境异常";
                        int severity = json.has("severity") ? json.get("severity").asInt() : 2;
                        String content = json.has("content") ? json.get("content").asText() : "传感器阈值超标";
                        
                        IotAlarm alarm = new IotAlarm();
                        alarm.setDeviceId(deviceId);
                        alarm.setAlarmType(alarmType);
                        alarm.setSeverity(severity);
                        alarm.setContent(content);
                        alarm.setStatus(0); // 未处理
                        alarm.setRelatedId(deviceId);
                        alarm.setAlarmSource("device");
                        alarm.setCreateTime(LocalDateTime.now());
                        
                        CompletableFuture.runAsync(() -> {
                            try {
                                iotAlarmService.insert(alarm);      // 插入数据库
                                log.info("[报警] 设备: {} | 类型: {}", deviceId, alarmType);
                            } catch (Exception e) {
                                log.error("保存{}的报警失败", deviceId, e);
                            }
                        });
                        return; // 报警事件单独处理，不走传感器数据存储逻辑
                    }

                    // 2. 处理传感器数据
                    IotSensorData data = new IotSensorData();
                    data.setDeviceId(deviceId);
                    data.setReportTime(LocalDateTime.now());

                    if (json.has("temperature")) data.setTemperature(BigDecimal.valueOf(json.get("temperature").asDouble()));
                    if (json.has("humidity")) data.setHumidity(BigDecimal.valueOf(json.get("humidity").asDouble()));
                    if (json.has("pressure")) data.setPressure(BigDecimal.valueOf(json.get("pressure").asDouble()));
                    if (json.has("smoke")) data.setSmokeDensity(BigDecimal.valueOf(json.get("smoke").asDouble()));
                    if (json.has("lux")) data.setLuminance(BigDecimal.valueOf(json.get("lux").asDouble()));
                    if (json.has("dewPoint")) data.setDewPoint(BigDecimal.valueOf(json.get("dewPoint").asDouble()));
                    
                    ObjectNode extra = objectMapper.createObjectNode();
                    if (json.has("alarm")) extra.put("alarm", json.get("alarm").asBoolean());
                    if (json.has("led")) extra.put("led", json.get("led").asBoolean());
                    
                    // 检查是否上传了 workMode 或 alarmMode，如果有则更新 device_config
                    if (json.has("workMode") || json.has("alarmMode")) {
                        // 异步更新配置表，不阻塞消息处理
                        JsonNode finalJson = json;
                        String finalMac = deviceId;
                        CompletableFuture.runAsync(() -> {
                            try {
                                boolean updated = false;
                                IotDeviceConfig config = iotDeviceConfigService.getByDeviceId(finalMac);
                                
                                if (config == null) {
                                    // 若不存在记录则不自动创建，以免覆盖默认值，或者您希望自动创建也可以
                                    // 这里假设设备必须先在系统注册并生成了默认配置
                                    return;
                                }

                                if (finalJson.has("workMode")) {
                                    String wm = finalJson.get("workMode").asText();
                                    // 仅当值不同且非空时更新
                                    if (wm != null && !wm.equals(config.getWorkMode())) {
                                        config.setWorkMode(wm);
                                        updated = true;
                                    }
                                }
                                if (finalJson.has("alarmMode")) {
                                    String am = finalJson.get("alarmMode").asText();
                                    if (am != null && !am.equals(config.getAlarmMode())) {
                                        config.setAlarmMode(am);
                                        updated = true;
                                    }
                                }

                                if (updated) {
                                    iotDeviceConfigService.updateConfig(config);
                                    log.info("从MQTT数据同步设备配置: {} -> Mode={}, Alarm={}", finalMac, config.getWorkMode(), config.getAlarmMode());
                                }
                            } catch (Exception e) {
                                log.error("同步设备配置失败: {}", finalMac, e);
                            }
                        });
                    }

                    // 只有当有额外信息时才设置，避免空或者是空对象占位
                    if (extra.size() > 0) {
                        data.setExtraInfo(extra.toString());
                    }

                    // 实时缓存：每条传感器消息都先更新内存，供 latest 接口低延迟读取
                    sensorRealtimeCache.put(deviceId, data);

                    // 智能存储策略 (Deadband & Throttling)
                    boolean shouldSave = false;
                    String reason = "";
                    IotSensorData last = lastSavedData.get(deviceId);

                    // 如果有led/alarm等状态变化，即使温湿度没变也要存！
                    boolean statusChanged = false;

                    if (last != null) {
                        try {
                            String lastExtra = last.getExtraInfo(); // 以JSON字符串形式存储
                            JsonNode lastExtraJson = lastExtra != null ? objectMapper.readTree(lastExtra) : objectMapper.createObjectNode();
                            
                            boolean lastLed = lastExtraJson.has("led") ? lastExtraJson.get("led").asBoolean() : false;
                            boolean currentLed = extra.has("led") ? extra.get("led").asBoolean() : false;
                            
                            boolean lastAlarm = lastExtraJson.has("alarm") ? lastExtraJson.get("alarm").asBoolean() : false;
                            boolean currentAlarm = extra.has("alarm") ? extra.get("alarm").asBoolean() : false;

                            // 仅在当前消息包含该字段时才比较
                            if (extra.has("led")) {
                                // 如果has("led")为true，currentLed已在上面正确定义
                                if (lastLed != currentLed) {
                                    statusChanged = true;
                                    String finalMac = deviceId;
                                    boolean finalLed = currentLed;
                                    CompletableFuture.runAsync(() -> 
                                        iotSwitchLogService.log(finalMac, "led", finalLed ? "ON" : "OFF", "hardware", null)
                                    );
                                }
                            }
                            
                            if (extra.has("alarm")) {
                                if (lastAlarm != currentAlarm) {
                                    statusChanged = true;
                                    String finalMac = deviceId;
                                    boolean finalAlarm = currentAlarm;
                                    CompletableFuture.runAsync(() -> 
                                        iotSwitchLogService.log(finalMac, "alarm", finalAlarm ? "ON" : "OFF", "hardware", null)
                                    );
                                }
                            }
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    } else {
                         // 首次遇到，检查是否为纯命令（仅led/alarm）
                         if (!json.has("temperature") && (json.has("led") || json.has("alarm"))) {
                             String finalMac = deviceId;
                             if (json.has("led")) {
                                 boolean led = json.get("led").asBoolean();
                                 CompletableFuture.runAsync(() -> 
                                     iotSwitchLogService.log(finalMac, "led", led ? "ON" : "OFF", "hardware", null)
                                 );
                             }
                        }
                    }
                    // 数值剧烈变化检测 (死区控制)
                    boolean valueChanged = false;
                    if (last != null && !statusChanged) {
                        // 1. 温度 (变化 > 0.5)
                        if (checkChange(last.getTemperature(), data.getTemperature(), 0.5)) {
                            valueChanged = true; reason = "温度变化 > 0.5";
                        }
                        // 2. 湿度 (变化 > 5.0)
                        if (!valueChanged && checkChange(last.getHumidity(), data.getHumidity(), 5.0)) {
                            valueChanged = true; reason = "湿度变化 > 5%";
                        }
                        // 3. 气压 (变化 > 2.0 hPa)
                        if (!valueChanged && checkChange(last.getPressure(), data.getPressure(), 2.0)) {
                            valueChanged = true; reason = "气压变化 > 2.0";
                        }
                        // 4. 烟雾 (变化 > 500.0，或者是模拟值底噪，调大阈值)
                        if (!valueChanged && checkChange(last.getSmokeDensity(), data.getSmokeDensity(), 500.0)) {
                            valueChanged = true; reason = "烟雾变化 > 500";
                        }
                        // 5. 露点 (变化 > 0.5)
                        if (!valueChanged && checkChange(last.getDewPoint(), data.getDewPoint(), 0.5)) {
                            valueChanged = true; reason = "露点变化 > 0.5";
                        }
                        
                        // 光照强度检测
                        if (!valueChanged && checkChange(last.getLuminance(), data.getLuminance(), 50.0)) {
                            valueChanged = true; reason = "亮度变化 > 50";
                        }
                    }

                    if (last == null) {
                        shouldSave = true; // 第一次，必存
                        reason = "首次数据";
                    } else if (statusChanged) {
                        shouldSave = true;
                        reason = "状态更新(LED/报警)";
                    } else if (valueChanged) {
                        shouldSave = true;
                        // 原因已在上面设定
                    } else {
                        // 时间判断
                        if (last.getReportTime() != null) {
                            long millisDiff = java.time.Duration.between(last.getReportTime(), data.getReportTime()).toMillis();
                            
                            // 变化不明显时，按配置的最小间隔入库（默认 2000ms）
                            if (millisDiff >= persistMinIntervalMs) {
                                shouldSave = true;
                                reason = "时间检查(>最小间隔)";
                            } else {
                                shouldSave = false;
                                reason = "限流(<最小间隔)";
                            }
                        } else {
                             // 如果last != null不应该发生，但作为安全防护
                             shouldSave = true;
                             reason = "安全防护";
                        }
                    }

                    if (shouldSave) {
                        // 仅在保存时更新内存状态
                        // 修复"滑动窗口"bug，即每1秒更新重置计时器
                        lastSavedData.put(deviceId, data);
                        
                        // 异步数据库操作
                        String finalReason = reason;
                        String finalMac = deviceId;
                        
                        CompletableFuture.runAsync(() -> {
                            try {
                                
                                iotDeviceService.updateDeviceStatus(finalMac, 1);
                                iotSensorDataMapper.insert(data);
                                log.info("[保存] {} | 原因: {}", finalMac, finalReason);
                            } catch (Exception e) {
                                log.error("异步保存{}失败", finalMac, e);
                            }
                        });
                    } else {
                        log.debug("[跳过] {} | 原因: {}", deviceId, reason);
                    }
                    
                } catch (Exception e) {
                    log.error("MQTT消息解析错误: {}", e.getMessage(), e);
                }
    }

    private boolean checkChange(BigDecimal v1, BigDecimal v2, double threshold) {
        if (v1 == null || v2 == null) return false;
        return v1.subtract(v2).abs().doubleValue() > threshold;
    }

    /**
     * 将写卡缓存中的预登记字段应用到 stock_item
     */
    private void applyCacheToItem(
            com.hstk.iot_warehouse.model.entity.WmsStockItem item,
            RfidWriteCache.Entry cached) {
        if (cached == null) return;
        if (cached.getQuantity() != null)   item.setQuantity(cached.getQuantity());
        if (cached.getLocation() != null
                && !cached.getLocation().isEmpty()) item.setLocation(cached.getLocation());
        if (cached.getBatchNo() != null
                && !cached.getBatchNo().isEmpty()) item.setBatchNo(cached.getBatchNo());
        if (cached.getExpiryDate() != null) item.setExpiryDate(cached.getExpiryDate());
    }
}
