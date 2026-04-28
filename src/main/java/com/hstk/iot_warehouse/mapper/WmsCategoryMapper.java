package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.WmsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物资分类Mapper接口
 */
@Mapper
public interface WmsCategoryMapper {
    /**
     * 插入分类
     * @param category 分类对象
     * @return 影响行数
     */
    int insert(WmsCategory category);

    /**
     * 更新分类
     * @param category 分类对象
     * @return 影响行数
     */
    int update(WmsCategory category);

    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 影响行数
     */
    int deleteById(Long categoryId);

    /**
     * 根据ID查询分类
     * @param categoryId 分类ID
     * @return 分类对象
     */
    WmsCategory selectById(Long categoryId);

    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<WmsCategory> selectAll();
}