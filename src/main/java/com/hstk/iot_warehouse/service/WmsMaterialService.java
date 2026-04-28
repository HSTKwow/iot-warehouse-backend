package com.hstk.iot_warehouse.service;

import com.hstk.iot_warehouse.model.entity.WmsMaterial;
import com.hstk.iot_warehouse.model.vo.WmsMaterialVO;
import com.hstk.iot_warehouse.model.vo.WmsMaterialDetailVO;
import com.hstk.iot_warehouse.common.api.PageResult;
import java.util.List;

public interface WmsMaterialService {

    /**
     * 新增物资
     * @param material 物资对象
     * @return 影响行数
     */
    int add(WmsMaterial material);

    /**
     * 更新物资信息
     * @param material 物资对象
     * @return 影响行数
     */
    int update(WmsMaterial material);

    /**
     * 批量或单条删除物资
     * @param materialIds 物资ID列表
     * @return 影响行数
     */
    int deleteByIds(List<Long> materialIds);

    /**
     * 根据ID获取物资详情（包含库存列表）
     */
    WmsMaterialDetailVO getDetailById(Long materialId);

    /**
     * 分页查询物资
     * @param page 页码
     * @param pageSize 每页条数
     * @param categoryId 分类ID
     * @param keyword 关键词
     */
    PageResult<WmsMaterialVO> getPage(int page, int pageSize, Long categoryId, String keyword);

    /**
     * 统计低库存物资数
     */
    long countLowStock();
}
