package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.IotDeviceConfigMapper;
import com.hstk.iot_warehouse.model.entity.IotDeviceConfig;
import com.hstk.iot_warehouse.service.IotDeviceConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IotDeviceConfigServiceImpl implements IotDeviceConfigService {

    @Autowired
    private IotDeviceConfigMapper iotDeviceConfigMapper;

    @Override
    public IotDeviceConfig getByDeviceId(String deviceId) {
        return iotDeviceConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public IotDeviceConfig updateConfig(IotDeviceConfig config) {
        IotDeviceConfig existing = iotDeviceConfigMapper.selectByDeviceId(config.getDeviceId());
        if (existing == null) {
            iotDeviceConfigMapper.insert(config);
        } else {
            // Ensure configId is set for update
            config.setConfigId(existing.getConfigId());
            iotDeviceConfigMapper.update(config);
        }
        return iotDeviceConfigMapper.selectByDeviceId(config.getDeviceId());
    }
}
