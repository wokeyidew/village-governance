package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 季度快照实体类
 * 对应表：quarterly_snapshot
 * 用于存储每季度每户的积分汇总及排名数据
 *
 * @author system
 * @since 2026-08-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("quarterly_snapshot")
public class QuarterlySnapshot {

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Integer tenantId;

    /**
     * 季度标识，如：2026-Q3
     */
    @TableField("quarter")
    private String quarter;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 本季获得积分（仅加分项总和）
     */
    @TableField("quarter_earned_points")
    private Integer quarterEarnedPoints;

    /**
     * 本季净积分（含扣分和整改恢复）
     */
    @TableField("quarter_net_points")
    private Integer quarterNetPoints;

    /**
     * 上季获得积分
     */
    @TableField("previous_quarter_points")
    private Integer previousQuarterPoints;

    /**
     * 进步分（本季 - 上季）
     */
    @TableField("progress_points")
    private Integer progressPoints;

    /**
     * 积分排名（1=最高）
     */
    @TableField("rank_points")
    private Integer rankPoints;

    /**
     * 进步排名（1=进步最大）
     */
    @TableField("rank_progress")
    private Integer rankProgress;

    /**
     * 标签：red（红榜）/ progress（蜕变榜）/ normal（普通）/ warning（帮扶对象）
     */
    @TableField("tag")
    private String tag;

    /**
     * 当季加分事项数量（去重，用于同名次排序）
     */
    @TableField("rule_count")
    private Integer ruleCount;

    /**
     * 当季活动参与次数（用于同名次排序）
     */
    @TableField("activity_count")
    private Integer activityCount;

    /**
     * 当季连续零扣分天数（用于同名次排序）
     */
    @TableField("no_penalty_days")
    private Integer noPenaltyDays;

    /**
     * 本季最后一次积分变动时间（用于同分排序）
     */
    @TableField("last_activity_time")
    private LocalDateTime lastActivityTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 非数据库字段（v2.0.9 新增） ====================

    /**
     * 用户姓名（非数据库字段，通过 user_id 关联 user 表查询获得）
     * 用于季度榜单展示，避免前端额外调用用户信息接口
     */
    @TableField(exist = false)
    private String userName;

}