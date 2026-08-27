package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动参与记录实体类
 * 对应表：activity_participation
 * 用于记录村民参与村集体活动的明细数据，支持活动参与次数的统计和排名
 *
 * 活动类型说明：
 * - regular: 常规活动（如培训讲座、集体劳动等）
 * - event: 专项评比活动（如美丽庭院评比、文明家庭评选等）
 * - emergency: 紧急活动（如抢险救灾、应急响应等）
 *
 * @author system
 * @since 2026-08-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_participation")
public class ActivityParticipation {

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
     * 参与用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 关联活动ID（activity表主键）
     * 可为空，支持手动录入活动名称的场景
     */
    @TableField("activity_id")
    private Integer activityId;

    /**
     * 活动名称
     */
    @TableField("activity_name")
    private String activityName;

    /**
     * 活动类型编码
     * regular: 常规活动
     * event: 专项评比活动
     * emergency: 紧急活动
     */
    @TableField("activity_type")
    private String activityType;

    /**
     * 获得积分
     */
    @TableField("earned_points")
    private Integer earnedPoints;

    /**
     * 参与时间
     */
    @TableField("participate_time")
    private LocalDateTime participateTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ==================== 活动类型常量 ====================

    /**
     * 活动类型：常规活动
     * 如培训讲座、集体劳动、文化活动、志愿服务等
     */
    public static final String TYPE_REGULAR = "regular";

    /**
     * 活动类型：专项评比活动
     * 如美丽庭院评比、文明家庭评选、红榜季度评比等
     */
    public static final String TYPE_EVENT = "event";

    /**
     * 活动类型：紧急活动
     * 如抢险救灾、应急响应、突发事件处理等
     */
    public static final String TYPE_EMERGENCY = "emergency";

}