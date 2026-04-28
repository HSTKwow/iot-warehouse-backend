package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.IotSwitchLogMapper;
import com.hstk.iot_warehouse.model.entity.IotSwitchLog;
import com.hstk.iot_warehouse.service.IotSwitchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IotSwitchLogServiceImpl implements IotSwitchLogService {

    @Autowired
    private IotSwitchLogMapper iotSwitchLogMapper;

    @Override
    public void log(String deviceId, String componentName, String action, String triggerSource, Long userId) {
        IotSwitchLog log = new IotSwitchLog();
        log.setDeviceId(deviceId);
        log.setComponentName(componentName);
        log.setAction(action);
        log.setTriggerSource(triggerSource);
        log.setUserId(userId);
        log.setStatus(1); // Default to success for now
        log.setCreateTime(LocalDateTime.now());
        
        iotSwitchLogMapper.insert(log);
    }

    @Override
    public List<IotSwitchLog> getRecentLogs(int limit) {
        return iotSwitchLogMapper.selectRecent(limit);
    }
}
