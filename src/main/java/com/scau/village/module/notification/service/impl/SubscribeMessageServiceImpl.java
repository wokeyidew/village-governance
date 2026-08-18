package com.scau.village.module.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.utils.WechatTemplateUtil;
import com.scau.village.module.notification.entity.SubscribeMessage;
import com.scau.village.module.notification.mapper.SubscribeMessageMapper;
import com.scau.village.module.notification.service.SubscribeMessageService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 微信订阅消息服务实现类
 *
 * @author system
 * @since 2026-07-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeMessageServiceImpl implements SubscribeMessageService {

    private final SubscribeMessageMapper subscribeMessageMapper;
    private final UserMapper userMapper;
    private final WechatTemplateUtil wechatTemplateUtil;

    @Override
    @Transactional
    public SubscribeMessage subscribe(Integer userId, Integer tenantId, String openid, String templateId) {
        // 检查是否已存在记录
        SubscribeMessage existing = subscribeMessageMapper.selectByUserIdAndTemplateId(userId, templateId);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                log.debug("用户已订阅该模板，userId={}, templateId={}", userId, templateId);
                return existing;
            } else {
                // 已取消订阅，重新激活
                existing.setStatus(1);
                existing.setUpdateTime(LocalDateTime.now());
                subscribeMessageMapper.updateById(existing);
                log.info("重新订阅成功，userId={}, templateId={}", userId, templateId);
                return existing;
            }
        }

        // 新建订阅记录
        SubscribeMessage record = new SubscribeMessage();
        record.setUserId(userId);
        record.setTenantId(tenantId);
        record.setOpenid(openid);
        record.setTemplateId(templateId);
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        subscribeMessageMapper.insert(record);
        log.info("订阅成功，userId={}, templateId={}", userId, templateId);
        return record;
    }

    @Override
    @Transactional
    public boolean unsubscribe(Integer userId, String templateId) {
        SubscribeMessage record = subscribeMessageMapper.selectByUserIdAndTemplateId(userId, templateId);
        if (record == null) {
            log.warn("取消订阅失败，记录不存在，userId={}, templateId={}", userId, templateId);
            return false;
        }
        if (record.getStatus() == 0) {
            log.debug("已处于取消状态，userId={}, templateId={}", userId, templateId);
            return true;
        }
        record.setStatus(0);
        record.setUpdateTime(LocalDateTime.now());
        subscribeMessageMapper.updateById(record);
        log.info("取消订阅成功，userId={}, templateId={}", userId, templateId);
        return true;
    }

    @Override
    public boolean isSubscribed(Integer userId, String templateId) {
        SubscribeMessage record = subscribeMessageMapper.selectByUserIdAndTemplateId(userId, templateId);
        return record != null && record.getStatus() == 1;
    }

    @Override
    public List<SubscribeMessage> getSubscribersByTemplateId(String templateId) {
        return subscribeMessageMapper.selectByTemplateId(templateId);
    }

    @Override
    public List<SubscribeMessage> getSubscribersByTenantId(Integer tenantId) {
        return subscribeMessageMapper.selectByTenantId(tenantId);
    }

    @Override
    public boolean sendSubscribeMessage(String openid, String templateId, String pagePath, Object data) {
        try {
            wechatTemplateUtil.sendSubscribeMessage(openid, templateId, pagePath, data);
            log.info("订阅消息发送成功，openid={}, templateId={}", openid, templateId);
            return true;
        } catch (Exception e) {
            log.error("订阅消息发送失败，openid={}, templateId={}, error={}", openid, templateId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int batchSendSubscribeMessage(String templateId, String pagePath, Object data) {
        List<SubscribeMessage> subscribers = getSubscribersByTemplateId(templateId);
        if (subscribers == null || subscribers.isEmpty()) {
            log.info("无订阅该模板的用户，templateId={}", templateId);
            return 0;
        }
        int successCount = 0;
        for (SubscribeMessage sub : subscribers) {
            // 只发送给状态为1的订阅者
            if (sub.getStatus() != 1) {
                continue;
            }
            boolean success = sendSubscribeMessage(sub.getOpenid(), templateId, pagePath, data);
            if (success) {
                successCount++;
            }
            // 可加入适当延迟防止微信限流
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("批量发送订阅消息完成，总发送={}, 成功={}, templateId={}", subscribers.size(), successCount, templateId);
        return successCount;
    }
}