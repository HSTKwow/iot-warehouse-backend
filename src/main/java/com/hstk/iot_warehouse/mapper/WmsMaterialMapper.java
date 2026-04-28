package com.hstk.iot_warehouse.mapper;

import com.hstk.iot_warehouse.model.entity.WmsMaterial;
import com.hstk.iot_warehouse.model.vo.WmsMaterialVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WmsMaterialMapper {

    int insert(WmsMaterial material);
    int update(WmsMaterial material);
    int deleteByIds(List<Long> materialIds);
    WmsMaterialVO selectById(Long materialId);
    WmsMaterial selectByCode(String materialCode);
    long count(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
    List<WmsMaterialVO> selectByPage(@Param("limit") int limit, @Param("offset") int offset, @Param("categoryId") Long categoryId, @Param("keyword") String keyword);
}