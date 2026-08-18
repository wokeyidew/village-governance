package com.scau.village.module.rectification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 整改任务实体类
 * 对应表名：rectification_task
 * 用于记录扣分后自动生成的整改任务，跟踪整改全过程
 *
 * @author system
 * @since 2026-08-19
 */
@Data
@TableName("rectification_task")
public class RectificationTask {

    /**
     * 主键ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联积分申请/评分记录ID（points_apply.id）
     */
    private Long applyId;

    /**
     * 责任户主用户ID（关联user表）
     */
    private Long userId;

    /**
     * 关联检查批次ID（inspection_batch.id）
     */
    private Long batchId;

    /**
     * 扣分规则名称（冗余存储，便于快速展示）
     */
    private String ruleName;

    /**
     * 整改要求（系统自动生成，如"请清理庭院垃圾"）
     */
    private String requirement;

    /**
     * 整改截止时间
     */
    private LocalDateTime deadline;

    /**
     * 任务状态：
     * pending-待整改，reviewing-待复核，resolved-已销项，overdue-逾期
     */
    private String status;

    /**
     * 整改前照片（即原扣分证据照片，逗号分隔）
     */
    private String beforePhotos;

    /**
     * 整改后照片（村民提交整改时上传，逗号分隔）
     */
    private String afterPhotos;

    /**
     * 村民提交整改时间
     */
    private LocalDateTime submitTime;

    /**
     * 复核结果：passed-通过，rejected-不通过
     */
    private String reviewResult;

    /**
     * 复核人ID（管理员）
     */
    private Long reviewerId;

    /**
     * 复核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 整改奖励积分（即扣分值的50%，恢复的积分）
     */
    private Integer rewardPoints;

    /**
     * 村民整改说明（可选）
     */
    private String submitRemark;

    /**
     * 复核备注（管理员填写）
     */
    private String reviewRemark;

    /**
     * 检查人ID（管理员）
     * 即创建整改任务时的评分人/检查人
     */
    private Long inspectorId;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    // ==================== 状态常量 ====================

    /**
     * 状态：待整改
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 状态：待复核（村民已提交整改）
     */
    public static final String STATUS_REVIEWING = "reviewing";

    /**
     * 状态：已销项（复核通过）
     */
    public static final String STATUS_RESOLVED = "resolved";

    /**
     * 状态：逾期（超过截止时间未整改）
     */
    public static final String STATUS_OVERDUE = "overdue";

    /**
     * 复核结果：通过
     */
    public static final String REVIEW_PASSED = "passed";

    /**
     * 复核结果：不通过
     */
    public static final String REVIEW_REJECTED = "rejected";

    // ==================== 便捷方法 ====================

    /**
     * 判断任务是否已逾期
     */
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(deadline) && !STATUS_RESOLVED.equals(status);
    }

    /**
     * 判断任务是否可以提交整改
     */
    public boolean canSubmit() {
        return STATUS_PENDING.equals(status) || STATUS_REVIEWING.equals(status);
    }

    /**
     * 判断任务是否可以复核
     */
    public boolean canReview() {
        return STATUS_REVIEWING.equals(status);
    }
}