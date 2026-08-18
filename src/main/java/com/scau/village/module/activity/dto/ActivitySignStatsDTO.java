package com.scau.village.module.activity.dto;

import lombok.Data;

/**
 * 活动签到统计数据传输对象
 * 用于管理员查看某个活动的签到/签退整体统计数据
 *
 * @author system
 * @since 2026-08-14
 */
@Data
public class ActivitySignStatsDTO {

    /**
     * 活动ID
     */
    private Integer activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 总报名人数
     */
    private Integer totalRegistrations;

    /**
     * 已签到人数（signed_in = 1）
     */
    private Integer signedInCount;

    /**
     * 已签退人数（checked_out = 1）
     */
    private Integer checkedOutCount;

    /**
     * 签到率（百分比，已签到人数 / 总报名人数 * 100）
     * 如：70.0 表示 70%
     */
    private Double signInRate;

    /**
     * 签退率（百分比，已签退人数 / 总报名人数 * 100）
     * 如：50.0 表示 50%
     */
    private Double checkOutRate;
}