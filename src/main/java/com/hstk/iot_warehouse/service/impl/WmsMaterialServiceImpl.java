package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.common.api.PageResult;
import com.hstk.iot_warehouse.component.event.StockAlarmCheckEvent;
import com.hstk.iot_warehouse.component.OssComponent;
import com.hstk.iot_warehouse.mapper.WmsMaterialMapper;
import com.hstk.iot_warehouse.mapper.WmsStockItemMapper;
import com.hstk.iot_warehouse.model.entity.WmsMaterial;
import com.hstk.iot_warehouse.model.vo.WmsMaterialVO;
import com.hstk.iot_warehouse.model.vo.WmsMaterialDetailVO;
import com.hstk.iot_warehouse.model.vo.WmsStockItemVO;
import com.hstk.iot_warehouse.service.WmsMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class WmsMaterialServiceImpl implements WmsMaterialService {

    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Autowired
    private WmsStockItemMapper wmsStockItemMapper;

    @Autowired
    private OssComponent ossComponent;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 新增物资，自动填充创建和更新时间
     */
    @Override
    public int add(WmsMaterial material) {
        if (material.getMaterialCode() == null || material.getMaterialCode().isEmpty()) {
            // Uniquely generate code if not provided
            material.setMaterialCode("MAT-" + System.currentTimeMillis());
        }
        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        wmsMaterialMapper.insert(material);
        // 新增物资后立即发布一次告警检查事件
        try {
            if (material.getMaterialId() != null) {
                eventPublisher.publishEvent(new StockAlarmCheckEvent(material.getMaterialId(), "material_add"));
            } else {
                eventPublisher.publishEvent(new StockAlarmCheckEvent(null, "material_add_fallback"));
            }
        } catch (Exception e) {
            log.error("Failed to check alarm after material add", e);
        }
        return 1;
    }

    /**
     * 更新物资信息
     */
    @Override
    public int update(WmsMaterial material) {
        // 如果是从有图变成另一张图（或无图），需要删除旧图
        if (material.getMaterialId() != null) {
            WmsMaterialVO oldMaterial = wmsMaterialMapper.selectById(material.getMaterialId());
            if (oldMaterial != null) {
                String oldUrl = oldMaterial.getImageUrl();
                String newUrl = material.getImageUrl();

                // 如果旧图存在，且新图不等于旧图（说明发生了修改），则删除旧图
                // 注意：这里假设前端传来的 material 包含了 imageUrl 字段。
                // 如果是部分更新且前端没传 imageUrl，则 newUrl 为 null，这里可能会误判？
                // 通常 update 接口传的是完整对象或者前端清楚当前状态。
                // 如果 newUrl 是 null，且数据库里有值，MyBatis 的 updateIfNotNull 策略通常会保留原值。
                // 但如果业务逻辑是 "替换图片"，前端一定会传新的 URL。
                // 只有当 newUrl != null 且不等于 oldUrl 时，我们才确定是用户有意修改了图片。
                if (oldUrl != null && !oldUrl.isEmpty() 
                        && newUrl != null && !newUrl.equals(oldUrl)) {
                    try {
                        ossComponent.deleteFile(oldUrl);
                    } catch (Exception e) {
                        // 仅记录日志，不阻断
                        // log.warn("Failed to delete old OSS file: {}", oldUrl);
                    }
                }
            }
        }

        material.setUpdateTime(LocalDateTime.now());
        int rows = wmsMaterialMapper.update(material);
        if (rows > 0 && material.getMaterialId() != null) {
            // 更新阈值后发布告警检查事件
            try {
                eventPublisher.publishEvent(new StockAlarmCheckEvent(material.getMaterialId(), "material_update"));
            } catch (Exception e) {
                log.error("Failed to check alarm after material update", e);
            }
        }
        return rows;
    }


    @Override
    public int deleteByIds(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return 0;
        }

        //检查这些物资下是否有在库库存 
        int stockCount = wmsStockItemMapper.countByMaterialIds(materialIds);
        if (stockCount > 0) {
            throw new RuntimeException("操作失败：选中的物资中存在尚有库存数据的物资，请清理库存后再试！");
        }

        //遍历删除 OSS 图片
        for (Long id : materialIds) {
            try {
                // 查询物资详情，获取 imageUrl
                WmsMaterialVO material = wmsMaterialMapper.selectById(id);
                if (material != null && material.getImageUrl() != null && !material.getImageUrl().isEmpty()) {
                    ossComponent.deleteFile(material.getImageUrl());
                    log.info("删除oss文件"+material.getImageUrl());
                }
            } catch (Exception e) {
                // 图片删除失败不应阻断主业务，仅记录日志
                // log.warn("Failed to delete OSS file for material {}", id, e);
            }
        }

        //批量删除
        return wmsMaterialMapper.deleteByIds(materialIds);
    }

    @Override
    public WmsMaterialDetailVO getDetailById(Long materialId) {
        // 1. Get Material Info (VO)
        WmsMaterialVO materialVO = wmsMaterialMapper.selectById(materialId);
        if (materialVO == null) {
            return null;
        }

        // 2. Wrap into Detail VO
        WmsMaterialDetailVO detailVO = new WmsMaterialDetailVO();
        BeanUtils.copyProperties(materialVO, detailVO);

        // 3. Fetch Stock Items
        List<WmsStockItemVO> stockItems = wmsStockItemMapper.selectByMaterialId(materialId);
        detailVO.setStockItems(stockItems);
        
        return detailVO;
    }

    @Override
    public PageResult<WmsMaterialVO> getPage(int page, int pageSize, Long categoryId, String keyword) {
        int offset = (page - 1) * pageSize;
        long total = wmsMaterialMapper.count(categoryId, keyword);
        List<WmsMaterialVO> list = wmsMaterialMapper.selectByPage(pageSize, offset, categoryId, keyword);
        return new PageResult<>(total, list);
    }

    @Override
    public long countLowStock() {
        // Fetch valid amount of items
        List<WmsMaterialVO> list = wmsMaterialMapper.selectByPage(1000, 0, null, null);
        return list.stream()
                .filter(m -> m.getCurrentStock() != null && m.getMinStock() != null && m.getCurrentStock() < m.getMinStock())
                .count();
    }
}
