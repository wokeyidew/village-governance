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
 * @author system
 * @since 2026-08-18
 */
@Data
@TableName("score_evidence")
public class ScoreEvidence {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联积分申请/评分记录ID（points_apply.id）
     */
    private Long applyId;

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
     */
    private Long inspectorId;

    /**
     * 关联检查批次ID（inspection_batch.id）
     */
    private Long batchId;

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
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}