package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.WmsLocation;
import java.util.List;

public interface WmsLocationService {

    /**
     * 获取所有位置信息
     * @return 位置列表
     */
    List<WmsLocation> getAllLocations();

    /**
     * 新增位置
     * @param location 位置信息
     */
    void addLocation(WmsLocation location);
    
    /**
     * 更新位置
     * @param location 位置信息
     */
    void updateLocation(WmsLocation location);
    
    /**
     * 删除位置
     * @param id 位置ID
     */
    void deleteLocation(String id);
}
