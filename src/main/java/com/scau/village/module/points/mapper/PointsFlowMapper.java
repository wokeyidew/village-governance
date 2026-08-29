package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.vo.PointsFlowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

/**
 * 积分流水 Mapper 接口
 *
 * @author system
 * @since 2026-07-18
 */
@Mapper
public interface PointsFlowMapper extends BaseMapper<PointsFlow> {

    /**
     * 分页查询用户的积分流水（含关联批次信息）
     * 用于积分明细页面展示，关联 points_apply 表获取图片和描述
     *
     * 修复说明（v2.0.9）：
     * 1. source_id 使用 String 类型接收，避免雪花 ID 精度丢失
     * 2. LEFT JOIN 增加 pa.tenant_id = pf.tenant_id 多租户隔离
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 分页的积分流水视图对象
     */
    @Select("SELECT " +
            "pf.id, " +
            "pf.user_id, " +
            "pf.change_amount, " +
            "pf.source_type, " +
            "pf.source_id, " +
            "pf.remark, " +
            "pf.create_time, " +
            "pf.batch_id, " +
            "pf.batch_name, " +
            "pf.apply_id, " +
            "pa.images, " +
            "pa.description " +
            "FROM points_flow pf " +
            "LEFT JOIN points_apply pa ON pf.source_id = pa.id " +
            "    AND pf.source_type IN ('admin_inspection', 'apply') " +
            "    AND pa.tenant_id = pf.tenant_id " +
            "WHERE pf.user_id = #{userId} " +
            "  AND pf.tenant_id = #{tenantId} " +
            "ORDER BY pf.create_time DESC")
    @Results(id = "pointsFlowVOMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "change_amount", property = "changeAmount"),
            @Result(column = "source_type", property = "sourceType"),
            // 【修复】source_id 映射到 String 类型的 sourceId
            @Result(column = "source_id", property = "sourceId"),
            @Result(column = "remark", property = "remark"),
            @Result(column = "create_time", property = "createTime"),
            // 来自 points_apply 表的字段
            @Result(column = "images", property = "images"),
            @Result(column = "description", property = "description"),
            // v2.0 新增字段
            @Result(column = "batch_id", property = "batchId"),
            @Result(column = "batch_name", property = "batchName"),
            @Result(column = "apply_id", property = "applyId")
    })
    Page<PointsFlowVO> selectFlowPage(Page<PointsFlow> page,
                                       @Param("userId") Integer userId,
                                       @Param("tenantId") Integer tenantId);
}