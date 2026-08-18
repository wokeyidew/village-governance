package com.scau.village.module.activity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名详情视图对象
 * 用于管理员查看某个活动下所有报名人员的签到/签退明细
 *
 * @author system
 * @since 2026-08-14
 */
@Data
public class RegistrationDetailVO {

    /**
     * 报名记录ID
     */
    private Integer registrationId;

    /**
     * 报名用户ID
     */
    private Integer userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String phone;

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
     * 参与时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 报名时填写的备注信息
     */
    private String remark;
}