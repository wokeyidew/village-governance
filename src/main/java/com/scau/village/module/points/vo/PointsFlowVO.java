package com.scau.village.module.points.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分流水视图对象
 * 用于前端展示积分变动记录，包含来源类型、关联批次等信息
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class PointsFlowVO {

    /**
     * 流水记录ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 变动积分值（正数增加，负数减少）
     */
    private Integer changeAmount;

    /**
     * 来源类型编码
     * - apply: 积分申报审核通过
     * - admin_inspection: 管理员现场评分
     * - exchange: 商品兑换
     * - activity: 活动签退奖励
     */
    private String sourceType;

    /**
     * 来源类型中文描述
     */
    private String sourceTypeText;

    // ================================================================
    // 【修改点】sourceId 类型：Integer → String（v2.0.9 修复）
    // 原因：数据库 source_id 为 varchar(64)，存储雪花 ID 字符串
    // 若保持 Integer，前端 JavaScript 处理 19 位数字会丢失精度
    // 关联表：points_apply.id（雪花ID，19位数字）
    // ================================================================
    /**
     * 来源记录ID（关联到对应表的ID，雪花ID字符串）
     * 注意：雪花 ID 为 19 位数字，必须用 String 类型
     * 关联 points_apply.id（雪花ID）
     * 前端可直接使用，无需额外处理精度
     */
    private String sourceId;

    /**
     * 变动备注
     */
    private String remark;

    /**
     * 关联的图片URL（若有）
     */
    private String images;

    /**
     * 关联的描述（如积分申报描述、活动名称等）
     */
    private String description;

    /**
     * 创建时间（变动时间）
     */
    private LocalDateTime createTime;

    // ==================== 新增字段（2026-08-25） ====================

    /**
     * 关联的检查批次ID（字符串格式，避免前端精度丢失）
     * 当来源为管理员现场评分或申报关联检查批次时存在，否则为 null
     */
    private String batchId;

    /**
     * 关联的检查批次名称
     * 当来源为管理员现场评分或申报关联检查批次时存在，否则为 null
     */
    private String batchName;

    // ==================== 新增字段（2026-08-27） ====================

    /**
     * 关联的积分申请/评分记录ID（points_apply.id）
     * 用于前端跳转申诉时传递 applyId，关联整改和申诉流程
     * 仅当 sourceType = 'admin_inspection' 或 'apply' 时存在，否则为 null
     * 字符串格式，避免前端 JavaScript 处理 19 位雪花 ID 时精度丢失
     */
    private String applyId;
}