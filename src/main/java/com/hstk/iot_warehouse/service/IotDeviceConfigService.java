package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.IotDeviceConfig;

/**
 * Device configuration service interface
 */
public interface IotDeviceConfigService {

    /**
     * Get device configuration by device ID
     * @param deviceId Device ID
     * @return Device configuration
     */
    IotDeviceConfig getByDeviceId(String deviceId);

    /**
     * Update device configuration
     * @param config Device configuration
     * @return Updated configuration
     */
    IotDeviceConfig updateConfig(IotDeviceConfig config);
}
