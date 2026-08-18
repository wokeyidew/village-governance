package com.scau.village.module.points.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查记录展示VO
 * 用于历史记录列表的返回数据
 *
 * @author system
 * @since 2026-07-16
 */
@Data
public class InspectionRecordVO {

    /**
     * 记录ID（对应points_apply.id）
     */
    private Long recordId;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 批次名称
     */
    private String batchName;

    /**
     * 户主用户ID
     */
    private Integer userId;

    /**
     * 户主姓名（关联user表查询）
     */
    private String userName;

    /**
     * 户主手机号（脱敏处理）
     */
    private String userPhone;

    /**
     * 规则ID
     */
    private Integer ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 该条规则得分（正数为加分，负数为扣分）
     */
    private Integer ruleScore;

    /**
     * 来源类型：admin-管理员评分，user-村民申报
     */
    private String sourceType;

    /**
     * 检查人ID（管理员ID）
     */
    private Integer inspectorId;

    /**
     * 检查人姓名（关联user表查询）
     */
    private String inspectorName;

    /**
     * 检查日期（格式：yyyy-MM-dd）
     */
    private LocalDate inspectionDate;

    /**
     * 备注/描述
     */
    private String description;

    /**
     * 图片（逗号分隔的URL）
     */
    private String images;

    /**
     * 状态（如：已通过、待审核等）
     */
    private String status;

    /**
     * 创建时间（提交时间）
     */
    private LocalDateTime createTime;
}