package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.WmsCategory;
import java.util.List;

public interface WmsCategoryService {
    /**
     * 获取所有分类
     * @return 分类列表
     */
    List<WmsCategory> getAll();

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类信息
     */
    WmsCategory getById(Long id);

    /**
     * 新增分类
     * @param category 分类对象
     * @return 影响行数
     */
    int add(WmsCategory category);

    /**
     * 更新分类
     * @param category 分类对象
     * @return 影响行数
     */
    int update(WmsCategory category);

    /**
     * 删除分类
     * @param id 分类ID
     * @return 影响行数
     */
    int delete(Long id);
}
