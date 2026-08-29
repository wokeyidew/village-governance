package com.scau.village.module.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.activity.dto.ActivitySignStatsDTO;
import com.scau.village.module.activity.dto.RegistrationDetailVO;
import com.scau.village.module.activity.entity.Activity;
import com.scau.village.module.activity.entity.ActivityRegistration;
import com.scau.village.module.activity.mapper.ActivityMapper;
import com.scau.village.module.activity.mapper.ActivityRegistrationMapper;
import com.scau.village.module.activity.service.ActivityRegistrationService;
import com.scau.village.module.log.service.OperationLogService;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动报名服务实现类
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> implements ActivityRegistrationService {

    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;
    private final PointsFlowMapper pointsFlowMapper;
    private final OperationLogService operationLogService;

    @Override
    @Transactional
    public void register(Long userId, Integer activityId, String name, String phone, String remark) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            throw new BusinessException("活动不存在或未开始");
        }
        // 检查是否已报名
        long count = lambdaQuery()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getUserId, userId)
                .count();
        if (count > 0) {
            throw new BusinessException("您已经报名过此活动");
        }
        ActivityRegistration reg = new ActivityRegistration();
        reg.setTenantId(activity.getTenantId());
        reg.setActivityId(activityId);
        reg.setUserId(userId.intValue());
        reg.setParticipantName(name);
        reg.setPhone(phone);
        reg.setRemark(remark);
        reg.setSignedIn(0);
        reg.setCheckedOut(0);
        reg.setCreateTime(LocalDateTime.now());
        save(reg);
    }

    /**
     * 取消报名
     * 规则：
     * 1. 只能取消自己的报名
     * 2. 活动未开始才能取消
     * 3. 活动开始前24小时内不可取消
     * 4. 活动已结束不可取消
     */
    @Override
    @Transactional
    public void cancel(Long userId, Integer registrationId) {
        // 1. 检查报名记录是否存在
        ActivityRegistration reg = getById(registrationId);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }

        // 2. 校验权限：只能取消自己的报名
        if (!reg.getUserId().equals(userId.intValue())) {
            throw new BusinessException("无权操作此报名");
        }

        // 3. 检查活动是否存在
        Activity activity = activityMapper.selectById(reg.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        // 4. 检查活动是否已开始（不允许取消已开始的活动）
        if (activity.getStartTime() != null && activity.getStartTime().isBefore(now)) {
            throw new BusinessException("活动已开始，无法取消报名");
        }

        // 5. 检查活动是否已结束（不允许取消已结束的活动）
        if (activity.getEndTime() != null && activity.getEndTime().isBefore(now)) {
            throw new BusinessException("活动已结束，无法取消报名");
        }

        // 6. 检查是否在24小时窗口内（活动开始前24小时内不可取消）
        if (activity.getStartTime() != null && 
            activity.getStartTime().isBefore(now.plusHours(24))) {
            throw new BusinessException("活动开始前24小时内不可取消");
        }

        // 7. 删除报名记录
        removeById(registrationId);
    }

    /**
     * 签到（仅记录签到时间，不发放积分）
     */
    @Override
    @Transactional
    public void signIn(Integer registrationId, Long operatorId) {
        ActivityRegistration reg = getById(registrationId);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (reg.getSignedIn() == 1) {
            throw new BusinessException("已签到，请勿重复操作");
        }
        Activity activity = activityMapper.selectById(reg.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        reg.setSignedIn(1);
        reg.setSignTime(now);
        updateById(reg);

        operationLogService.log(operatorId, "ACTIVITY_SIGNIN",
                String.format("签到成功，报名记录ID:%d，用户ID:%d", registrationId, reg.getUserId()));
        log.info("签到成功，registrationId={}, userId={}", registrationId, reg.getUserId());
    }

    /**
     * 签退（记录签退时间，计算参与时长，发放活动奖励积分）
     */
    @Override
    @Transactional
    public void checkout(Integer registrationId, Long operatorId) {
        ActivityRegistration reg = getById(registrationId);
        if (reg == null) {
            throw new BusinessException("报名记录不存在");
        }
        if (reg.getSignedIn() != 1) {
            throw new BusinessException("用户尚未签到，无法签退");
        }
        if (reg.getCheckedOut() == 1) {
            throw new BusinessException("已签退，请勿重复操作");
        }

        Activity activity = activityMapper.selectById(reg.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查活动是否已结束（允许活动结束后一定时间内签退，例如2小时内）
        // 若活动尚未结束，也允许签退（提前离场），但可根据业务需要限制
        // 这里放宽限制：只要当前时间在活动开始之后即可签退
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动尚未开始，无法签退");
        }

        // 计算参与时长（分钟）
        long minutes = 0;
        if (reg.getSignTime() != null) {
            minutes = Duration.between(reg.getSignTime(), now).toMinutes();
            if (minutes < 0) minutes = 0;
        }

        // 更新签退信息
        reg.setCheckedOut(1);
        reg.setCheckoutTime(now);
        reg.setDurationMinutes((int) minutes);
        updateById(reg);

        // 发放积分（如果活动有奖励积分且大于0）
        if (activity.getRewardPoints() != null && activity.getRewardPoints() > 0) {
            User user = userMapper.selectById(reg.getUserId());
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // ================================================================
            // 【修复点1】同步更新三个积分字段（v2.0 双轨制）
            // ================================================================
            int rewardPoints = activity.getRewardPoints();
            user.setPoints(user.getPoints() + rewardPoints);
            user.setTotalEarnedPoints(user.getTotalEarnedPoints() + rewardPoints);
            user.setAvailablePoints(user.getAvailablePoints() + rewardPoints);
            userMapper.updateById(user);

            PointsFlow flow = new PointsFlow();
            flow.setUserId(user.getId());
            flow.setChangeAmount(rewardPoints);
            flow.setSourceType("activity");
            // ================================================================
            // 【修复点2】sourceId 转为 String
            // ================================================================
            flow.setSourceId(activity.getId().toString());
            flow.setRemark(String.format("活动签退奖励：%s（参与%d分钟）", activity.getTitle(), minutes));
            flow.setCreateTime(now);
            pointsFlowMapper.insert(flow);

            operationLogService.log(operatorId, "ACTIVITY_CHECKOUT",
                    String.format("签退成功，用户 %s 获得 %d 积分，参与时长 %d 分钟，报名记录ID:%d",
                            user.getRealName(), rewardPoints, minutes, registrationId));
        } else {
            operationLogService.log(operatorId, "ACTIVITY_CHECKOUT",
                    String.format("签退成功，用户ID:%d，参与时长 %d 分钟，无积分奖励", reg.getUserId(), minutes));
        }

        log.info("签退成功，registrationId={}, userId={}, duration={}分钟", registrationId, reg.getUserId(), minutes);
    }

    @Override
    public List<ActivityRegistration> getByUserId(Integer userId) {
        return lambdaQuery()
                .eq(ActivityRegistration::getUserId, userId)
                .orderByDesc(ActivityRegistration::getCreateTime)
                .list();
    }

    // ==================== 管理员签到统计（新增） ====================

    @Override
    public ActivitySignStatsDTO getSignStatsByActivityId(Integer activityId, Integer tenantId) {
        return baseMapper.selectSignStatsByActivityId(activityId, tenantId);
    }

    @Override
    public List<RegistrationDetailVO> getRegistrationDetailsByActivityId(Integer activityId, Integer tenantId) {
        return baseMapper.selectRegistrationDetailsByActivityId(activityId, tenantId);
    }
}