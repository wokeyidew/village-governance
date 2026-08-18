package com.scau.village.module.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动实体类
 * 对应表名：activity
 *
 * @author system
 * @since 2026-07-17
 */
@Data
@TableName("activity")
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户ID（所属村庄）
     */
    private Integer tenantId;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动开始时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 签到奖励积分（0表示无积分奖励）
     */
    private Integer rewardPoints;

    /**
     * 活动描述/详情
     */
    private String description;

    /**
     * 最大参与人数（0表示不限人数）
     */
    private Integer maxParticipants;

    /**
     * 活动状态
     * 0-未开始，1-进行中，2-已结束
     */
    private Integer status;

    /**
     * 创建时间（发布时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}