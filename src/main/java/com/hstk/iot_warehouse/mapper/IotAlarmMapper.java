package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.IotAlarm;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物联网告警Mapper接口
 */
@Mapper
public interface IotAlarmMapper {
    /**
     * 插入告警
     * @param alarm 告警对象
     * @return 影响行数
     */
    int insert(IotAlarm alarm);

    /**
     * 更新告警
     * @param alarm 告警对象
     * @return 影响行数
     */
    int update(IotAlarm alarm);

    /**
     * 根据ID删除
     * @param alarmId 告警ID
     * @return 影响行数
     */
    int deleteById(Long alarmId);

    /**
     * 根据ID查询
     * @param alarmId 告警ID
     * @return 告警对象
     */
    IotAlarm selectById(Long alarmId);

    /**
     * 根据设备ID查询告警
     * @param deviceId 设备ID
     * @return 告警列表
     */
    List<IotAlarm> selectByDeviceId(String deviceId);

    /**
     * 根据来源和关联ID查询
     * @param alarmSource 来源
     * @param relatedId 关联ID
     * @return 告警列表
     */
    List<IotAlarm> selectBySourceAndRelatedId(String alarmSource, String relatedId);

    /**
     * 查询最近告警
     * @param limit 条数限制
     * @return 告警列表
     */
    List<IotAlarm> selectRecent(int limit);

    /**
     * 根据告警类型查询
     * @param alarmType 告警类型
     * @return 告警列表
     */
    List<IotAlarm> selectByAlarmType(String alarmType);

    /**
     * 根据告警来源查询
     * @param alarmSource 告警来源
     * @return 告警列表
     */
    List<IotAlarm> selectByAlarmSource(String alarmSource);

    /**
     * 更新告警状态
     * @param alarmId 告警ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(Long alarmId, Integer status);

    /**
     * 处理告警(含处理时间、备注和处理人)
     */
    int resolveAlarm(Long alarmId, LocalDateTime resolveTime, String resolveRemark, String resolveUser);

    /**
     * 查询高优先级告警
     * @param limit 条数限制
     * @return 告警列表
     */
    List<IotAlarm> selectHighPriorityAlarms(int limit);

    /**
     * 查询未处理告警
     * @return 告警列表
     */
    List<IotAlarm> selectUnprocessed();

    /**
     * 查询某设备未处理告警
     * @param deviceId 设备ID
     * @return 告警列表
     */
    List<IotAlarm> selectUnprocessedByDeviceId(String deviceId);

    /**
     * 按用户设备过滤查询最近告警(含库存告警)
     * @param userId 用户ID
     * @param limit 条数
     * @return 告警列表
     */
    List<IotAlarm> selectRecentByUserId(Long userId, int limit);

    /**
     * 按来源和状态查询告警
     * @param alarmSource 告警来源
     * @param status 状态(0=未处理, 1=已处理), 为null时不过滤状态
     * @return 告警列表
     */
    List<IotAlarm> selectBySourceAndStatus(@org.apache.ibatis.annotations.Param("alarmSource") String alarmSource,
                                           @org.apache.ibatis.annotations.Param("status") Integer status);
}