package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水实体类
 * 对应表：points_flow
 *
 * @author system
 * @since 2026-07-18
 */
@Data
@TableName("points_flow")
public class PointsFlow {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer changeAmount;

    private String sourceType;

    // ================================================================
    // 【修改点】sourceId 类型：Integer → String（v2.0.9 修复）
    // 原因：数据库 source_id 为 varchar(64)，存储雪花 ID 字符串
    // 若保持 Integer，写入 19 位雪花 ID 会超出范围导致报错
    // 关联表：points_apply.id（雪花ID，19位数字）
    // ================================================================
    /**
     * 来源记录ID（关联到对应表的ID，雪花ID字符串）
     * 注意：雪花 ID 为 19 位数字，必须用 String 类型
     * 关联 points_apply.id（雪花ID）
     */
    private String sourceId;

    private String remark;

    private LocalDateTime createTime;

    private Integer tenantId;

    // ==================== v2.0 新增字段 ====================

    /**
     * 关联检查批次ID（雪花ID字符串）
     * 当来源为管理员现场评分时存在，否则为 null
     */
    private String batchId;

    /**
     * 关联检查批次名称
     * 当来源为管理员现场评分时存在，否则为 null
     */
    private String batchName;

    /**
     * 关联积分申请/评分记录ID（雪花ID字符串）
     * 用于前端申诉跳转，关联整改和申诉流程
     * 仅当 sourceType = 'admin_inspection' 或 'apply' 时存在
     */
    private String applyId;

}