package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.WmsIoRecordMapper;
import com.hstk.iot_warehouse.model.dto.IoTrendDTO;
import com.hstk.iot_warehouse.model.dto.IoTrendRowDTO;
import com.hstk.iot_warehouse.model.entity.WmsIoRecord;
import com.hstk.iot_warehouse.model.vo.WmsIoRecordVO;
import com.hstk.iot_warehouse.service.WmsIoRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class WmsIoRecordServiceImpl implements WmsIoRecordService {

    @Autowired
    private WmsIoRecordMapper wmsIoRecordMapper;

    @Override
    public void addRecord(String rfid, Integer type, String orderNo) {
        addRecord(rfid, type, orderNo, "WEB_CLIENT");
    }

    @Override
    public void addRecord(String rfid, Integer type, String orderNo, String deviceId) {
        WmsIoRecord record = new WmsIoRecord();
        record.setRfidTag(rfid);
        record.setIoType(type);
        record.setOrderNo(orderNo);
        record.setTime(LocalDateTime.now());
        record.setDeviceId(deviceId);
        wmsIoRecordMapper.insert(record);
    }

    @Override
    public List<WmsIoRecordVO> getAll() {
        return wmsIoRecordMapper.selectAllWithInfo(1000, 0);
    }

    @Override
    public com.hstk.iot_warehouse.common.api.PageResult<WmsIoRecordVO> getAll(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<WmsIoRecordVO> list = wmsIoRecordMapper.selectAllWithInfo(pageSize, offset);
        long total = wmsIoRecordMapper.countAll();
        return new com.hstk.iot_warehouse.common.api.PageResult<>(total, list);
    }

    @Override
    public long countToday() {
        List<WmsIoRecordVO> list = wmsIoRecordMapper.selectAllWithInfo(1000, 0);
        LocalDateTime startOfDay = LocalDateTime.now().with(java.time.LocalTime.MIN);
        // Note: VO likely has createTime or just time field? Let's check the entity
        // Entity has 'time', VO probably has 'time'.
        return list.stream()
                .filter(r -> r.getTime() != null && r.getTime().isAfter(startOfDay))
                .count();
    }

    @Override
    public long countWeek() {
        // Simple approximation
        List<WmsIoRecordVO> list = wmsIoRecordMapper.selectAllWithInfo(1000, 0);
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7).with(java.time.LocalTime.MIN);
        return list.stream()
                .filter(r -> r.getTime() != null && r.getTime().isAfter(startOfWeek))
                .count();
    }

    @Override
    public IoTrendDTO getIoTrend(Long materialId, String range) {
        if (materialId == null) {
            throw new IllegalArgumentException("materialId不能为空");
        }
        if (range == null || range.isBlank()) {
            range = "week";
        }
        if (!"week".equals(range) && !"month".equals(range) && !"year".equals(range)) {
            throw new IllegalArgumentException("range只能是 week/month/year");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startTime;
        LocalDateTime endTime;
        String groupType;

        if ("week".equals(range)) {
            startTime = today.minusDays(6).atStartOfDay();
            endTime = today.plusDays(1).atStartOfDay();
            groupType = "day";
        } else if ("month".equals(range)) {
            startTime = today.minusDays(29).atStartOfDay();
            endTime = today.plusDays(1).atStartOfDay();
            groupType = "day";
        } else {
            YearMonth currentMonth = YearMonth.now();
            startTime = currentMonth.minusMonths(11).atDay(1).atStartOfDay();
            endTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
            groupType = "month";
        }

        List<IoTrendRowDTO> rows = wmsIoRecordMapper.selectIoTrendByMaterial(materialId, startTime, endTime, groupType);

        LinkedHashMap<String, Long> inboundMap = new LinkedHashMap<>();
        LinkedHashMap<String, Long> outboundMap = new LinkedHashMap<>();

        if ("year".equals(range)) {
            YearMonth startMonth = YearMonth.from(startTime);
            for (int i = 0; i < 12; i++) {
                String key = startMonth.plusMonths(i).toString();
                inboundMap.put(key, 0L);
                outboundMap.put(key, 0L);
            }
        } else {
            LocalDate startDate = startTime.toLocalDate();
            long days = ChronoUnit.DAYS.between(startDate, endTime.toLocalDate());
            for (int i = 0; i < days; i++) {
                String key = startDate.plusDays(i).toString();
                inboundMap.put(key, 0L);
                outboundMap.put(key, 0L);
            }
        }

        for (IoTrendRowDTO row : rows) {
            if (row.getIoType() == null || row.getCount() == null) {
                continue;
            }

            if (row.getIoType() == 1) {
                inboundMap.put(row.getBucket(), row.getCount());
            } else if (row.getIoType() == 2) {
                outboundMap.put(row.getBucket(), row.getCount());
            }
        }

        IoTrendDTO dto = new IoTrendDTO();
        dto.setMaterialId(materialId);
        dto.setRange(range);
        dto.setXAxis(new ArrayList<>(inboundMap.keySet()));
        dto.setInbound(new ArrayList<>(inboundMap.values()));
        dto.setOutbound(new ArrayList<>(outboundMap.values()));
        return dto;
    }
}
