package com.scau.village.module.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.activity.dto.ActivitySignStatsDTO;
import com.scau.village.module.activity.dto.RegistrationDetailVO;
import com.scau.village.module.activity.entity.ActivityRegistration;

import java.util.List;

/**
 * 活动报名服务接口
 * @author system
 * @since 2026-07-17
 */
public interface ActivityRegistrationService extends IService<ActivityRegistration> {

    /**
     * 报名活动
     * @param userId 当前用户ID
     * @param activityId 活动ID
     * @param name 参与者姓名
     * @param phone 联系电话
     * @param remark 备注
     */
    void register(Long userId, Integer activityId, String name, String phone, String remark);

    /**
     * 取消报名
     * @param userId 当前用户ID（用于权限校验）
     * @param registrationId 报名记录ID
     */
    void cancel(Long userId, Integer registrationId);

    /**
     * 签到（记录签到时间，不发放积分）
     * @param registrationId 报名记录ID
     * @param operatorId 操作员ID（村委/网格员）
     */
    void signIn(Integer registrationId, Long operatorId);

    /**
     * 签退（记录签退时间，计算参与时长，发放活动奖励积分）
     * @param registrationId 报名记录ID
     * @param operatorId 操作员ID（村委/网格员）
     */
    void checkout(Integer registrationId, Long operatorId);

    /**
     * 获取当前用户的所有报名记录
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<ActivityRegistration> getByUserId(Integer userId);

    // ==================== 管理员签到统计（新增） ====================

    /**
     * 获取活动签到统计（管理员）
     * 返回报名人数、已签到、已签退、签到率、签退率
     *
     * @param activityId 活动ID
     * @param tenantId   租户ID
     * @return 统计数据对象，若无报名数据则返回各字段为0的统计对象
     */
    ActivitySignStatsDTO getSignStatsByActivityId(Integer activityId, Integer tenantId);

    /**
     * 获取活动报名明细列表（管理员查看谁签到/签退了）
     * 返回每个报名人的签到状态、签退状态、时间等
     *
     * @param activityId 活动ID
     * @param tenantId   租户ID
     * @return 报名明细列表，若无报名记录则返回空列表
     */
    List<RegistrationDetailVO> getRegistrationDetailsByActivityId(Integer activityId, Integer tenantId);
}