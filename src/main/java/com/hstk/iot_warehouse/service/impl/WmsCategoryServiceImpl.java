package com.hstk.iot_warehouse.service.impl;

import com.hstk.iot_warehouse.mapper.WmsCategoryMapper;
import com.hstk.iot_warehouse.mapper.WmsMaterialMapper;
import com.hstk.iot_warehouse.model.entity.WmsCategory;
import com.hstk.iot_warehouse.service.WmsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WmsCategoryServiceImpl implements WmsCategoryService {

    @Autowired
    private WmsCategoryMapper wmsCategoryMapper;

    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Override
    public List<WmsCategory> getAll() {
        return wmsCategoryMapper.selectAll();
    }

    @Override
    public WmsCategory getById(Long id) {
        return wmsCategoryMapper.selectById(id);
    }

    @Override
    public int add(WmsCategory category) {
        // No auto timestamp in current POJO, assumes DB default or not needed? 
        // WmsCategory POJO from read_file only had categoryId, categoryName, description.
        // Let's stick to that.
        wmsCategoryMapper.insert(category);
        return 1;
    }

    @Override
    public int update(WmsCategory category) {
        return wmsCategoryMapper.update(category);
    }

    @Override
    public int delete(Long id) {
        // Use the general count method with categoryId filter
        long count = wmsMaterialMapper.count(id, null);
        if (count > 0) {
            throw new RuntimeException("该分类下包含物资，请先将物资移动到其他分类后再删除");
        }
        return wmsCategoryMapper.deleteById(id);
    }
}
