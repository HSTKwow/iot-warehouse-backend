package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.IotAlarmMapper;
import com.hstk.iot_warehouse.model.entity.IotAlarm;
import com.hstk.iot_warehouse.service.IotAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IotAlarmServiceImpl implements IotAlarmService {

    @Autowired
    private IotAlarmMapper iotAlarmMapper;

    @Override
    public void insert(IotAlarm alarm) {
        iotAlarmMapper.insert(alarm);
    }

    @Override
    public List<IotAlarm> listAll(Long userId) {
        if (userId == null) {
            return iotAlarmMapper.selectRecent(500);
        }
        return iotAlarmMapper.selectRecentByUserId(userId, 500);
    }

    @Override
    public List<IotAlarm> listUnprocessed() {
        return iotAlarmMapper.selectUnprocessed();
    }

    @Override
    public List<IotAlarm> listByDeviceId(String deviceId) {
        return iotAlarmMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<IotAlarm> listBySource(String alarmSource) {
        return iotAlarmMapper.selectByAlarmSource(alarmSource);
    }

    @Override
    public List<IotAlarm> listBySourceAndStatus(String alarmSource, Integer status) {
        return iotAlarmMapper.selectBySourceAndStatus(alarmSource, status);
    }

    @Override
    public void resolve(Long alarmId) {
        iotAlarmMapper.updateStatus(alarmId, 1);
    }

    @Override
    public void resolveWithRemark(Long alarmId, String remark, String resolveUser) {
        iotAlarmMapper.resolveAlarm(alarmId, java.time.LocalDateTime.now(), remark, resolveUser);
    }

    @Override
    public int countUnprocessed() {
        List<IotAlarm> list = iotAlarmMapper.selectUnprocessed();
        return list != null ? list.size() : 0;
    }

    @Override
    public int countBySeverityAndUnprocessed(int severity) {
        List<IotAlarm> list = iotAlarmMapper.selectUnprocessed();
        if (list == null) return 0;
        return (int) list.stream().filter(a -> a.getSeverity() != null && a.getSeverity() == severity).count();
    }
}
