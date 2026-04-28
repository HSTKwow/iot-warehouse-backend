package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.WmsLocationMapper;
import com.hstk.iot_warehouse.model.entity.WmsLocation;
import com.hstk.iot_warehouse.service.WmsLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WmsLocationServiceImpl implements WmsLocationService {

    @Autowired
    private WmsLocationMapper locationMapper;

    @Override
    public List<WmsLocation> getAllLocations() {
        return locationMapper.selectAll();
    }

    @Override
    public void addLocation(WmsLocation location) {
        locationMapper.insert(location);
    }
    
    @Override
    public void updateLocation(WmsLocation location) {
         locationMapper.update(location);
    }
    
    @Override
    public void deleteLocation(String id) {
        locationMapper.deleteById(id);
    }
}
