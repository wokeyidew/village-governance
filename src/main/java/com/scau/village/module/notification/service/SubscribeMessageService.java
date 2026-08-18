package com.scau.village.module.notification.service;

import com.scau.village.module.notification.entity.SubscribeMessage;

import java.util.List;

/**
 * 微信订阅消息服务接口
 * 用于管理管理员对订阅消息的订阅状态及发送通知
 *
 * @author system
 * @since 2026-07-18
 */
public interface SubscribeMessageService {

    /**
     * 订阅消息（管理员订阅）
     *
     * @param userId     管理员用户ID
     * @param tenantId   租户ID
     * @param openid     管理员微信openid
     * @param templateId 微信模板ID
     * @return 订阅记录
     */
    SubscribeMessage subscribe(Integer userId, Integer tenantId, String openid, String templateId);

    /**
     * 取消订阅
     *
     * @param userId     管理员用户ID
     * @param templateId 微信模板ID
     * @return true-取消成功，false-取消失败
     */
    boolean unsubscribe(Integer userId, String templateId);

    /**
     * 检查用户是否已订阅指定模板
     *
     * @param userId     管理员用户ID
     * @param templateId 微信模板ID
     * @return true-已订阅，false-未订阅
     */
    boolean isSubscribed(Integer userId, String templateId);

    /**
     * 根据模板ID获取所有已订阅的管理员列表
     *
     * @param templateId 微信模板ID
     * @return 订阅记录列表
     */
    List<SubscribeMessage> getSubscribersByTemplateId(String templateId);

    /**
     * 根据租户ID获取所有已订阅的管理员列表
     *
     * @param tenantId 租户ID
     * @return 订阅记录列表
     */
    List<SubscribeMessage> getSubscribersByTenantId(Integer tenantId);

    /**
     * 发送订阅消息给单个用户
     *
     * @param openid     接收人openid
     * @param templateId 模板ID
     * @param pagePath   跳转小程序页面路径
     * @param data       模板数据（key-value格式）
     * @return true-发送成功，false-发送失败
     */
    boolean sendSubscribeMessage(String openid, String templateId, String pagePath, Object data);

    /**
     * 批量发送订阅消息给所有订阅了该模板的管理员
     *
     * @param templateId 模板ID
     * @param pagePath   跳转小程序页面路径
     * @param data       模板数据
     * @return 成功发送数量
     */
    int batchSendSubscribeMessage(String templateId, String pagePath, Object data);
}