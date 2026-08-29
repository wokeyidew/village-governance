package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.InspectionHousehold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 检查户汇总 Mapper 接口
 *
 * 修复说明（2026-08-30）：
 * - selectByBatchIdAndUserId 方法中 batchId 参数类型从 Long 改为 String，与实体类一致
 *
 * @author system
 * @since 2026-07-16
 */
@Mapper
public interface InspectionHouseholdMapper extends BaseMapper<InspectionHousehold> {

    /**
     * 根据批次ID和用户ID查询汇总记录（用于判断是否存在，进而执行插入或更新）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @param userId  用户ID（自增ID）
     * @return 汇总记录，不存在则返回 null
     */
    @Select("SELECT * FROM inspection_household WHERE batch_id = #{batchId} AND user_id = #{userId} AND deleted = 0")
    InspectionHousehold selectByBatchIdAndUserId(@Param("batchId") String batchId, 
                                                  @Param("userId") Integer userId);
}