package com.hstk.iot_warehouse.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        return options;
    }

    @Bean
    public IMqttClient mqttClient(MqttConnectOptions mqttConnectOptions) throws MqttException {
        // 保持 Client ID 在 23 字符以内，兼容所有 Broker
        String shortId;
        if (clientId.length() > 15) {
            shortId = clientId.substring(0, 15);
        } else {
            shortId = clientId;
        }
        String fullClientId = shortId + "_" + (System.currentTimeMillis() % 100000);

        org.eclipse.paho.client.mqttv3.persist.MemoryPersistence persistence =
                new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence();
        MqttClient mqttClient = new MqttClient(brokerUrl, fullClientId, persistence);

        // 连接失败不阻止应用启动，由 MqttListener 负责重连
        try {
            mqttClient.connect(mqttConnectOptions);
            log.info("MQTT 连接成功: broker={}, clientId={}", brokerUrl, fullClientId);
        } catch (MqttException e) {
            log.warn("MQTT 初始连接失败: {}", e.getMessage());
        }
        return mqttClient;
    }
}
