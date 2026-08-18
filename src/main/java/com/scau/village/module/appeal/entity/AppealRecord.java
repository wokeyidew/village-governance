package com.scau.village.module.appeal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 申诉记录实体类
 * 对应表名：appeal_record
 * 用于记录村民对扣分项提出的申诉，以及管理员的处理过程
 *
 * @author system
 * @since 2026-08-19
 */
@Data
@TableName("appeal_record")
public class AppealRecord {

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
     * 申诉人用户ID
     */
    private Long userId;

    /**
     * 申诉人姓名（冗余存储，便于快速展示）
     */
    private String userName;

    /**
     * 申诉理由（必填）
     */
    private String reason;

    /**
     * 补充证据照片（逗号分隔）
     */
    private String evidencePhotos;

    /**
     * 申诉状态：
     * pending-申诉中，resolved-已处理
     */
    private String status;

    /**
     * 复核决定：
     * upheld-维持原判，modified-修改评分，revoked-撤销评分
     */
    private String decision;

    /**
     * 处理说明（管理员填写）
     */
    private String decisionDetail;

    /**
     * 复核人ID（管理员）
     */
    private Long reviewerId;

    /**
     * 复核人姓名（冗余存储）
     */
    private String reviewerName;

    /**
     * 复核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 关联检查批次ID（便于快速查询）
     */
    private Long batchId;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 创建时间（申诉提交时间）
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
     * 状态：申诉中
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 状态：已处理
     */
    public static final String STATUS_RESOLVED = "resolved";

    /**
     * 决定：维持原判
     */
    public static final String DECISION_UPHELD = "upheld";

    /**
     * 决定：修改评分
     */
    public static final String DECISION_MODIFIED = "modified";

    /**
     * 决定：撤销评分
     */
    public static final String DECISION_REVOKED = "revoked";
}