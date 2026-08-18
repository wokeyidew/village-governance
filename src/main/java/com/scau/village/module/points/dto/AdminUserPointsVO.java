package com.scau.village.module.points.dto;

import lombok.Data;

/**
 * 管理员查询用户积分返回VO
 * 用于管理员端查看指定用户的积分信息
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class AdminUserPointsVO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号（脱敏显示）
     */
    private String phone;

    /**
     * 当前积分余额
     */
    private Integer points;

    /**
     * 村小组
     */
    private String villageGroup;

    /**
     * 用户角色
     */
    private String role;
}