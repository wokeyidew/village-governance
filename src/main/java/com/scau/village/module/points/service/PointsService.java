package com.scau.village.module.points.service;

import com.scau.village.module.points.dto.AdminUserPointsVO;

/**
 * 积分综合服务接口
 * 封装积分模块中涉及多个子模块（规则、申报、流水、用户）的综合业务逻辑
 *
 * @author system
 * @since 2026-07-18
 */
public interface PointsService {

    /**
     * 管理员根据用户ID查询用户积分信息
     *
     * @param userId 用户ID
     * @return 用户积分信息VO
     */
    AdminUserPointsVO getUserPointsByAdmin(Integer userId);

    /**
     * 管理员根据手机号查询用户积分信息
     *
     * @param phone 手机号
     * @return 用户积分信息VO
     */
    AdminUserPointsVO getUserPointsByPhone(String phone);
}