package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.service.WmsIoRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/io-records")
public class WmsIoRecordController {

    @Autowired
    private WmsIoRecordService wmsIoRecordService;

    /**
     * 获取所有出入库历史记录 (强制分页)
     */
    @GetMapping
    public Result<?> getIoRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(wmsIoRecordService.getAll(page, pageSize));
    }
}
