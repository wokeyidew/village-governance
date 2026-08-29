package com.scau.village.module.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.utils.QRCodeCacheUtil;
import com.scau.village.common.utils.QRCodeUtil;
import com.scau.village.module.activity.dto.QRCodeDto;
import com.scau.village.module.activity.entity.Activity;
import com.scau.village.module.activity.entity.ActivityQRCode;
import com.scau.village.module.activity.entity.ActivityRegistration;
import com.scau.village.module.activity.mapper.ActivityMapper;
import com.scau.village.module.activity.mapper.ActivityQRCodeMapper;
import com.scau.village.module.activity.mapper.ActivityRegistrationMapper;
import com.scau.village.module.activity.service.ActivityService;
import com.scau.village.module.activity.vo.QRCodeVO;
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
import java.util.UUID;

/**
 * 活动服务实现类
 * @author system
 * @since 2026-07-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    private final ActivityQRCodeMapper activityQRCodeMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final UserMapper userMapper;
    private final PointsFlowMapper pointsFlowMapper;
    private final OperationLogService operationLogService;
    private final QRCodeCacheUtil qrCodeCacheUtil;

    @Override
    @Transactional
    public QRCodeVO generateQRCode(Integer activityId, String type, Boolean isDynamic) {
        // 1. 校验活动是否存在且未结束
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getStatus() == 2) {
            throw new BusinessException("活动已结束，无法生成二维码");
        }

        // =========================================================
        // 修复：固定码每次生成不同，避免唯一索引冲突
        // 原逻辑：基于 activityId + type + 密钥 生成 MD5（每次相同）
        // 新逻辑：使用 UUID + 时间戳 生成唯一固定码
        // =========================================================
        // 2. 生成固定码（每次唯一，包含活动ID和类型信息，便于追溯）
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        // 格式：活动ID_类型_时间戳_随机数，确保唯一性
        String fixedCode = activityId + "_" + type + "_" + timestamp + "_" + random;

        // 3. 生成动态码key（如果动态码）
        String dynamicKey = null;
        String qrcodeContent;
        if (isDynamic) {
            dynamicKey = qrCodeCacheUtil.generateDynamicKey();
            qrcodeContent = QRCodeUtil.buildQRCodeContent(fixedCode, dynamicKey, type, activityId);
            // 缓存动态码，value = fixedCode:type:activityId
            qrCodeCacheUtil.saveDynamicCode(dynamicKey, fixedCode + ":" + type + ":" + activityId);
        } else {
            qrcodeContent = QRCodeUtil.buildQRCodeContent(fixedCode, null, type, activityId);
        }

        // 4. 保存二维码记录到数据库
        ActivityQRCode qrCode = new ActivityQRCode();
        qrCode.setActivityId(activityId);
        qrCode.setQrcodeType(type);
        qrCode.setQrcodeKey(dynamicKey);
        qrCode.setFixedCode(fixedCode);
        qrCode.setStatus(1);
        if (isDynamic) {
            qrCode.setExpireTime(LocalDateTime.now().plusSeconds(15));
        }
        activityQRCodeMapper.insert(qrCode);

        // 5. 生成二维码图片（Base64）
        String qrImage = QRCodeUtil.generateQRCodeBase64(qrcodeContent);

        // 6. 封装返回VO
        QRCodeVO vo = new QRCodeVO();
        vo.setQrcodeImage(qrImage);
        vo.setFixedCode(fixedCode);
        vo.setDynamicKey(dynamicKey);
        vo.setExpireSeconds(isDynamic ? 15 : 0);
        vo.setQrcodeType(type);
        vo.setIsDynamic(isDynamic);

        log.info("生成二维码成功，活动ID={}, 类型={}, 动态={}, fixedCode={}", activityId, type, isDynamic, fixedCode);
        return vo;
    }

    @Override
    @Transactional
    public String scanSignIn(String scanData, Integer userId) {
        return scanQRCode(scanData, userId, "signin");
    }

    @Override
    @Transactional
    public String scanCheckout(String scanData, Integer userId) {
        return scanQRCode(scanData, userId, "checkout");
    }

    /**
     * 统一扫码处理逻辑
     */
    private String scanQRCode(String scanData, Integer userId, String expectedType) {
        // 1. 解析扫码内容
        JSONObject data;
        try {
            data = JSON.parseObject(scanData);
        } catch (Exception e) {
            throw new BusinessException("无效的二维码数据");
        }
        String fixedCode = data.getString("fixed");
        String dynamicKey = data.getString("key");
        String type = data.getString("type");
        Integer activityId = data.getInteger("activityId");

        // 2. 验证类型是否匹配
        if (!expectedType.equals(type)) {
            throw new BusinessException("请使用正确的" + ("signin".equals(expectedType) ? "签到" : "签退") + "二维码");
        }

        // 3. 验证固定码是否存在且有效
        ActivityQRCode qrCode = activityQRCodeMapper.selectByFixedCodeAndType(fixedCode, type);
        if (qrCode == null || qrCode.getStatus() != 1) {
            throw new BusinessException("无效的二维码");
        }

        // 4. 验证活动是否存在且有效
        Activity activity = getById(activityId);
        if (activity == null || activity.getStatus() == 2) {
            throw new BusinessException("活动不存在或已结束");
        }

        // 5. 检查时间窗口
        LocalDateTime now = LocalDateTime.now();
        if ("signin".equals(type)) {
            // 签到：活动开始前30分钟至活动开始后30分钟
            LocalDateTime signStart = activity.getStartTime().minusMinutes(30);
            LocalDateTime signEnd = activity.getStartTime().plusMinutes(30);
            if (now.isBefore(signStart) || now.isAfter(signEnd)) {
                throw new BusinessException("不在签到时间内，签到窗口为活动开始前后30分钟");
            }
        } else {
            // 签退：活动结束前30分钟至活动结束后30分钟
            LocalDateTime checkoutStart = activity.getEndTime().minusMinutes(30);
            LocalDateTime checkoutEnd = activity.getEndTime().plusMinutes(30);
            if (now.isBefore(checkoutStart) || now.isAfter(checkoutEnd)) {
                throw new BusinessException("不在签退时间内，签退窗口为活动结束前后30分钟");
            }
        }

        // 6. 如果是动态码，验证动态码
        if (dynamicKey != null) {
            String expectedValue = fixedCode + ":" + type + ":" + activityId;
            boolean valid = qrCodeCacheUtil.validateDynamicCode(dynamicKey, expectedValue);
            if (!valid) {
                throw new BusinessException("二维码已过期，请刷新后重试");
            }
            // 使用后销毁动态码（防止重复使用）
            qrCodeCacheUtil.removeDynamicCode(dynamicKey);
        }

        // 7. 检查用户是否报名了该活动
        LambdaQueryWrapper<ActivityRegistration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getUserId, userId);
        ActivityRegistration registration = activityRegistrationMapper.selectOne(regWrapper);
        if (registration == null) {
            throw new BusinessException("您未报名此活动，无法扫码" + ("signin".equals(type) ? "签到" : "签退"));
        }

        // 8. 执行签到或签退
        if ("signin".equals(type)) {
            if (registration.getSignedIn() == 1) {
                throw new BusinessException("您已签到，请勿重复签到");
            }
            registration.setSignedIn(1);
            registration.setSignTime(now);
            activityRegistrationMapper.updateById(registration);
            operationLogService.log(userId.longValue(), "ACTIVITY_SIGNIN",
                    String.format("扫码签到成功，活动ID=%d, 用户ID=%d", activityId, userId));
            log.info("扫码签到成功，activityId={}, userId={}", activityId, userId);
            return "签到成功！";
        } else {
            // 签退
            if (registration.getSignedIn() != 1) {
                throw new BusinessException("您尚未签到，无法签退");
            }
            if (registration.getCheckedOut() == 1) {
                throw new BusinessException("您已签退，请勿重复签退");
            }
            // 计算参与时长（分钟）
            long minutes = 0;
            if (registration.getSignTime() != null) {
                minutes = Duration.between(registration.getSignTime(), now).toMinutes();
                if (minutes < 0) minutes = 0;
            }
            registration.setCheckedOut(1);
            registration.setCheckoutTime(now);
            registration.setDurationMinutes((int) minutes);
            activityRegistrationMapper.updateById(registration);

            // 发放积分（如果活动有奖励积分）
            if (activity.getRewardPoints() != null && activity.getRewardPoints() > 0) {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                // ================================================================
                // 【修复点】同步更新三个积分字段（v2.0 双轨制）
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
                // 【修复点】sourceId 转为 String
                // ================================================================
                flow.setSourceId(activity.getId().toString());
                flow.setRemark(String.format("活动签退奖励：%s（参与%d分钟）", activity.getTitle(), minutes));
                flow.setCreateTime(now);
                pointsFlowMapper.insert(flow);

                operationLogService.log(userId.longValue(), "ACTIVITY_CHECKOUT",
                        String.format("扫码签退成功，活动ID=%d, 用户ID=%d, 奖励积分=%d, 参与时长=%d分钟",
                                activityId, userId, rewardPoints, minutes));
                log.info("扫码签退成功，activityId={}, userId={}, 积分+{}, 时长={}分钟",
                        activityId, userId, rewardPoints, minutes);
                return "签退成功！奖励积分：" + rewardPoints + "，参与时长：" + minutes + "分钟";
            } else {
                operationLogService.log(userId.longValue(), "ACTIVITY_CHECKOUT",
                        String.format("扫码签退成功，活动ID=%d, 用户ID=%d, 无积分奖励", activityId, userId));
                log.info("扫码签退成功，activityId={}, userId={}, 无积分", activityId, userId);
                return "签退成功！参与时长：" + minutes + "分钟";
            }
        }
    }
}