package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.WmsLocation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 仓库库位Mapper接口
 */
@Mapper
public interface WmsLocationMapper {
    /**
     * 插入库位
     * @param location 库位对象
     * @return 影响行数
     */
    int insert(WmsLocation location);

    /**
     * 更新库位
     * @param location 库位对象
     * @return 影响行数
     */
    int update(WmsLocation location);

    /**
     * 删除库位
     * @param id 库位ID
     * @return 影响行数
     */
    int deleteById(String id);

    /**
     * 查询所有库位
     * @return 库位列表
     */
    List<WmsLocation> selectAll();
}
