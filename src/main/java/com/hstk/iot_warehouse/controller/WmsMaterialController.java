package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.PageResult;
import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.model.entity.WmsMaterial;
import com.hstk.iot_warehouse.model.vo.WmsMaterialVO;
import com.hstk.iot_warehouse.model.vo.WmsMaterialDetailVO;
import com.hstk.iot_warehouse.service.WmsMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/materials")
public class WmsMaterialController {

    @Autowired
    private WmsMaterialService wmsMaterialService;

    /**
     * 添加物资
     * @param material 物资对象
     * @return 操作结果
     */
    @PostMapping
    public Result<String> add(@RequestBody WmsMaterial material) {
        log.info("add material"+material.getMaterialId());
        wmsMaterialService.add(material);
        return Result.success("Material added successfully");
    }

    /**
     * 更新物资信息
     * @param material 物资对象
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody WmsMaterial material) {
        log.info("update material"+material.getMaterialId());
        wmsMaterialService.update(material);
        return Result.success("Material updated successfully");
    }

    /**
     * 根据ID批量或单条删除物资
     * @param ids 物资ID列表
     * @return 操作结果
     */
    @DeleteMapping("/{ids}")
    public Result<String> delete(@PathVariable List<Long> ids) {
        log.info("delete material: {}", ids);
        wmsMaterialService.deleteByIds(ids);
        return Result.success("Materials deleted successfully");
    }

    /**
     * 根据ID获取物资详情
     * @param id 物资ID
     * @return 物资对象（包含库存明细）
     */
    @GetMapping("/{id}")
    public Result<WmsMaterialDetailVO> getDetailById(@PathVariable Long id) {
        log.info("get material"+id);
        WmsMaterialDetailVO material = wmsMaterialService.getDetailById(id);
        return Result.success(material);
    }


    /**
     * 获取所有物资列表（支持分页、搜索、分类过滤）
     * @return 物资列表
     */
    @GetMapping
    public Result<?> getAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword
            ) {
        log.info("get materials page={}, pageSize={}, categoryId={}, keyword={}", page, pageSize, categoryId, keyword);
        PageResult<WmsMaterialVO> list = wmsMaterialService.getPage(page, pageSize, categoryId, keyword);
        return Result.success(list);
    }
}
