package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.WmsCategory;
import com.hstk.iot_warehouse.service.WmsCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/categories")
public class WmsCategoryController {

    @Autowired
    private WmsCategoryService wmsCategoryService;

    /**
     * 获取所有分类
     * @return 分类列表
     */
    @GetMapping
    public Result<List<WmsCategory>> getAllCategories() {
        log.info("获取所有分类");
        return Result.success(wmsCategoryService.getAll());
    }

    /**
     * 新增分类
     * @param category 分类对象
     * @return 结果
     */
    @PostMapping
    public Result<String> addCategory(@RequestBody WmsCategory category) {
        log.info("新增分类");
        wmsCategoryService.add(category);
        return Result.success("Category added successfully");
    }

    /**
     * 更新分类信息
     * @param category 分类对象
     * @return 结果
     */
    @PutMapping
    public Result<String> updateCategory(@RequestBody WmsCategory category) {
        log.info("更新分类信息");
        wmsCategoryService.update(category);
        return Result.success("Category updated successfully");
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        log.info("删除分类");
        wmsCategoryService.delete(id);
        return Result.success("Category deleted successfully");
    }
}
