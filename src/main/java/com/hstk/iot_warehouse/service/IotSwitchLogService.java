package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.IotSwitchLog;
import java.util.List;

public interface IotSwitchLogService {
    void log(String deviceId, String componentName, String action, String triggerSource, Long userId);
    List<IotSwitchLog> getRecentLogs(int limit);
}
