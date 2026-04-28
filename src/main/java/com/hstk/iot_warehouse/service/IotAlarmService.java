package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.IotAlarm;

import java.util.List;

public interface IotAlarmService {

    /** 插入告警 */
    void insert(IotAlarm alarm);

    /** 查询告警(按时间倒序), userId为null查全部, 否则按用户设备过滤 */
    List<IotAlarm> listAll(Long userId);

    /** 查询未处理告警 */
    List<IotAlarm> listUnprocessed();

    /** 根据设备查询 */
    List<IotAlarm> listByDeviceId(String deviceId);

    /** 根据来源查询 */
    List<IotAlarm> listBySource(String alarmSource);

    /** 根据来源和状态查询(status为null时不过滤) */
    List<IotAlarm> listBySourceAndStatus(String alarmSource, Integer status);

    /** 处理告警(更新状态) */
    void resolve(Long alarmId);

    /** 处理告警(含处理备注和处理人) */
    void resolveWithRemark(Long alarmId, String remark, String resolveUser);

    /** 统计: 未处理数量 */
    int countUnprocessed();

    /** 统计: 各级别未处理数量 */
    int countBySeverityAndUnprocessed(int severity);
}
