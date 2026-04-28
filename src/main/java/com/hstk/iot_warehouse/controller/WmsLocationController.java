package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.model.entity.WmsLocation;
import com.hstk.iot_warehouse.service.WmsLocationService;
import com.hstk.iot_warehouse.common.api.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@CrossOrigin
public class WmsLocationController {

    @Autowired
    private WmsLocationService locationService;

    /**
     * 获取所有位置信息
     * @return 位置列表
     */
    @GetMapping
    public Result<List<WmsLocation>> list() {
        return Result.success(locationService.getAllLocations());
    }

    /**
     * 新增位置
     * @param location 位置对象
     * @return 结果
     */
    @PostMapping
    public Result add(@RequestBody WmsLocation location) {
        locationService.addLocation(location);
        return Result.success();
    }
    
    /**
     * 更新位置信息
     * @param location 位置对象
     * @return 结果
     */
    @PutMapping
    public Result update(@RequestBody WmsLocation location) {
        locationService.updateLocation(location);
        return Result.success();
    }
    
    /**
     * 删除位置
     * @param id 位置ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable String id) {
        locationService.deleteLocation(id);
        return Result.success();
    }
}
