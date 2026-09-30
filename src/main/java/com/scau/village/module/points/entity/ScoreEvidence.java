package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评分证据实体类
 * 对应表名：score_evidence
 * 用于存储评分时绑定的证据（照片、时间、检查人等信息）
 * 仅扣分项强制绑定证据，加分项可选
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 字段类型从 Long 改为 String，避免前端 JavaScript 精度丢失
 * - 移除 @JsonSerialize(using = ToStringSerializer.class)，因为 String 类型不需要序列化处理
 * - 字段包括：applyId, inspectorId, batchId
 *
 * @author system
 * @since 2026-08-18
 */
@Data
@TableName("score_evidence")
public class ScoreEvidence {

    /**
     * 主键ID（雪花算法生成的字符串）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 关联积分申请/评分记录ID（points_apply.id）
     * 雪花ID字符串类型，避免前端精度丢失
     */
    private String applyId;

    /**
     * 照片URL列表（多张用逗号分隔）
     * 存储格式：/upload/evidence/20260818_xxx.jpg,/upload/evidence/20260818_yyy.jpg
     */
    private String photoUrls;

    /**
     * 照片数量
     */
    private Integer photoCount;

    /**
     * 拍摄位置（GPS坐标或手动输入的地址描述）
     */
    private String location;

    /**
     * 检查人ID（管理员ID）
     * 雪花ID字符串类型，避免前端精度丢失
     */
    private String inspectorId;

    /**
     * 关联检查批次ID（inspection_batch.id）
     * 雪花ID字符串类型，避免前端精度丢失
     */
    private String batchId;

    /**
     * 评分时使用的规则版本号
     * 用于追溯规则变更历史
     */
    private String ruleVersion;

    /**
     * 是否已添加水印：0-否，1-是
     */
    private Integer hasWatermark;

    /**
     * 扣分规则名称（冗余存储，便于快速展示）
     * 避免每次查询都关联 points_rule 表
     */
    private String ruleName;

    /**
     * 户主姓名（冗余存储，便于快速展示）
     * 避免每次查询都关联 user 表
     */
    private String userName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 租户ID（多租户隔离）
     * 冗余存储，便于按租户查询和数据隔离
     */
    private Integer tenantId;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    /** 观察结束时间，规则 #37 的复查证据使用。 */
    @TableField("observed_at")
    private LocalDateTime observedAt;

    /** 观察类型，例如规则 #37 的 recheck。 */
    @TableField("observation_type")
    private String observationType;
}
