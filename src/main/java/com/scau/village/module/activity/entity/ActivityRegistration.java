package com.scau.village.module.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动报名记录实体
 * 对应表名：activity_registration
 *
 * @author system
 * @since 2026-07-19
 */
@Data
@TableName("activity_registration")
public class ActivityRegistration {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer tenantId;

    private Integer activityId;

    private Integer userId;

    private String participantName;

    private String phone;

    private String remark;

    /**
     * 签到状态：0-未签到，1-已签到
     */
    private Integer signedIn;

    /**
     * 签到时间
     */
    private LocalDateTime signTime;

    /**
     * 签退状态：0-未签退，1-已签退
     */
    private Integer checkedOut;

    /**
     * 签退时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 参与时长（分钟），签退时计算
     */
    private Integer durationMinutes;

    /**
     * 报名时间
     */
    private LocalDateTime createTime;
}