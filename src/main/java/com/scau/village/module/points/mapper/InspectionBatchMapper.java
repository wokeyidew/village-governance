package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.InspectionBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 检查批次 Mapper 接口
 *
 * @author system
 * @since 2026-07-16
 */
@Mapper
public interface InspectionBatchMapper extends BaseMapper<InspectionBatch> {

    // 基础 CRUD 由 MyBatis-Plus 提供，无需额外编写
    // 如需复杂查询，可在此添加自定义方法及对应的 XML
}