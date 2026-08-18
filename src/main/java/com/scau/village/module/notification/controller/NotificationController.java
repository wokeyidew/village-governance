package com.scau.village.module.notification.controller;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.notification.entity.SubscribeMessage;
import com.scau.village.module.notification.service.SubscribeMessageService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 微信订阅消息控制器
 * 用于管理管理员的订阅状态
 *
 * @author system
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final SubscribeMessageService subscribeMessageService;
    private final UserService userService;

    /**
     * 订阅消息（管理员订阅）
     * 请求体：{ "templateId": "xxx" }
     */
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestBody Map<String, String> params) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        // 只有管理员才能订阅通知
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");

        String templateId = params.get("templateId");
        if (templateId == null || templateId.isEmpty()) {
            return Result.error(400, "模板ID不能为空");
        }

        Integer userId = ctx.getUserId().intValue();
        Integer tenantId = ctx.getTenantId();

        // 获取用户的 openid
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        String openid = user.getOpenid();
        if (openid == null || openid.isEmpty()) {
            return Result.error(400, "用户未绑定微信，无法订阅");
        }

        subscribeMessageService.subscribe(userId, tenantId, openid, templateId);
        return Result.success(null);
    }

    /**
     * 取消订阅
     * 请求体：{ "templateId": "xxx" }
     */
    @DeleteMapping("/unsubscribe")
    public Result<Void> unsubscribe(@RequestBody Map<String, String> params) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");

        String templateId = params.get("templateId");
        if (templateId == null || templateId.isEmpty()) {
            return Result.error(400, "模板ID不能为空");
        }

        Integer userId = ctx.getUserId().intValue();
        boolean result = subscribeMessageService.unsubscribe(userId, templateId);
        return result ? Result.success(null) : Result.error(500, "取消订阅失败");
    }

    /**
     * 检查当前用户是否已订阅指定模板
     */
    @GetMapping("/check/{templateId}")
    public Result<Boolean> checkSubscription(@PathVariable String templateId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer userId = ctx.getUserId().intValue();
        boolean subscribed = subscribeMessageService.isSubscribed(userId, templateId);
        return Result.success(subscribed);
    }

    /**
     * 管理员查询所有订阅者列表
     * 可按模板ID筛选（可选）
     */
    @GetMapping("/subscribers")
    public Result<List<SubscribeMessage>> getSubscribers(@RequestParam(required = false) String templateId) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");
        Integer tenantId = UserContext.get().getTenantId();
        List<SubscribeMessage> list;
        if (templateId != null && !templateId.isEmpty()) {
            list = subscribeMessageService.getSubscribersByTemplateId(templateId);
        } else {
            list = subscribeMessageService.getSubscribersByTenantId(tenantId);
        }
        return Result.success(list);
    }
}