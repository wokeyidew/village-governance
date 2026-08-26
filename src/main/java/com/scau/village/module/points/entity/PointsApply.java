package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 积分申请/评分记录实体类
 * 对应表名：points_apply
 * 既支持村民自主申报（source_type='user'），也支持管理员现场评分（source_type='admin'）
 *
 * @author system
 * @since 2026-07-16
 */
@Data
@TableName("points_apply")
public class PointsApply {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 用户ID（申报人或被评分的户主）
     */
    private Integer userId;

    /**
     * 积分规则ID（关联points_rule表）
     */
    private Integer ruleId;

    /**
     * 申报/评分描述
     */
    private String description;

    /**
     * 图片（多张用逗号分隔）
     */
    private String images;

    /**
     * 状态：待审核、已通过、已驳回等（村民申报流程使用）
     * 管理员评分时可直接设为'已通过'或'已生效'
     */
    private String status;

    /**
     * 审核人ID
     */
    private Integer auditorId;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 创建时间（申报时间或评分时间）
     */
    private LocalDateTime createTime;

    // ========== 以下为新增字段（适配管理员现场评分场景） ==========

    /**
     * 来源类型：user-村民申报，admin-管理员评分
     */
    @TableField(value = "source_type")
    private String sourceType;

    /**
     * 检查人ID（管理员ID），管理员评分时必填
     */
    @TableField(value = "inspector_id")
    private Integer inspectorId;

    /**
     * 检查批次ID（关联inspection_batch.id），管理员评分时必填
     * 雪花算法生成的 Long 类型，序列化为字符串避免前端 JS 精度丢失
     */
    @TableField(value = "inspection_batch_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionBatchId;

    /**
     * 检查日期（管理员评分时填写，格式：yyyy-MM-dd）
     */
    @TableField(value = "inspection_date")
    private LocalDate inspectionDate;

    // ========== 证据链相关字段（新增） ==========

    /**
     * 是否有关联的证据（扣分项强制有证据）
     * 0-无证据，1-有证据
     * 用于快速标识该记录是否可查看证据详情
     */
    @TableField(value = "has_evidence")
    private Integer hasEvidence;

}