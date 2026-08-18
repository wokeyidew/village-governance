package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.InspectionHousehold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 检查户汇总 Mapper 接口
 *
 * @author system
 * @since 2026-07-16
 */
@Mapper
public interface InspectionHouseholdMapper extends BaseMapper<InspectionHousehold> {

    /**
     * 根据批次ID和用户ID查询汇总记录（用于判断是否存在，进而执行插入或更新）
     *
     * @param batchId 批次ID
     * @param userId  用户ID
     * @return 汇总记录，不存在则返回 null
     */
    InspectionHousehold selectByBatchIdAndUserId(@Param("batchId") Long batchId, @Param("userId") Integer userId);

}