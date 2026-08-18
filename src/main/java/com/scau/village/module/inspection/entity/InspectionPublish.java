package com.scau.village.module.inspection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评比发布记录实体类
 * 对应表名：inspection_publish
 * 用于记录检查批次评比结果的发布状态
 *
 * @author system
 * @since 2026-07-18
 */
@Data
@TableName("inspection_publish")
public class InspectionPublish {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 检查批次ID（关联 inspection_batch 表）
     */
    private Long batchId;

    /**
     * 发布状态：draft-草稿（未发布），published-已发布
     */
    private String status;

    /**
     * 评比结果快照（JSON格式）
     * 包含所有户的得分明细，格式示例：
     * {
     *   "batchName": "2026年7月人居环境检查",
     *   "inspectionDate": "2026-07-20",
     *   "households": [
     *     {
     *       "userId": 1,
     *       "userName": "张三",
     *       "totalScore": 25,
     *       "details": [
     *         {"ruleName": "庭院地面干净整洁", "score": 10},
     *         {"ruleName": "垃圾分类", "score": 15}
     *       ]
     *     }
     *   ]
     * }
     */
    private String resultJson;

    /**
     * 发布时间（状态变为 published 时记录）
     */
    private LocalDateTime publishedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}