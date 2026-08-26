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

    /**
     * 来源记录ID（关联到对应表的ID）
     */
    private Integer sourceId;

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
}