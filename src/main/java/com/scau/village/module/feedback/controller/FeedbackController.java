package com.scau.village.module.feedback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.feedback.dto.FeedbackReplyDto;
import com.scau.village.module.feedback.dto.FeedbackSubmitDto;
import com.scau.village.module.feedback.service.FeedbackService;
import com.scau.village.module.feedback.vo.FeedbackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 意见反馈控制器
 * 提供村民端和管理端的反馈相关接口
 *
 * @author system
 * @since 2026-07-23
 */
@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ==================== 村民端接口 ====================

    /**
     * 提交反馈（村民）
     * 需要登录，角色不限
     */
    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody FeedbackSubmitDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        feedbackService.submit(ctx.getUserId().intValue(), ctx.getTenantId(), dto);
        return Result.success(null);
    }

    /**
     * 获取当前用户的反馈列表（村民）
     * 分页返回当前登录用户提交的所有反馈
     */
    @GetMapping("/my-list")
    public Result<Page<FeedbackVO>> myList(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Page<FeedbackVO> result = feedbackService.getMyFeedbackList(ctx.getUserId().intValue(), page, size);
        return Result.success(result);
    }

    /**
     * 获取反馈详情（村民/管理员通用）
     * 村民只能查看自己的，管理员可查看所有
     */
    @GetMapping("/detail/{id}")
    public Result<FeedbackVO> detail(@PathVariable Integer id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        String role = ctx.getRole();
        FeedbackVO vo = feedbackService.getFeedbackDetail(id, ctx.getUserId().intValue(), role);
        return Result.success(vo);
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员获取所有反馈列表
     * 支持按状态筛选，分页返回
     */
    @GetMapping("/admin/list")
    public Result<Page<FeedbackVO>> adminList(@RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Page<FeedbackVO> result = feedbackService.getAdminFeedbackList(ctx.getTenantId(), status, page, size);
        return Result.success(result);
    }

    /**
     * 管理员回复反馈
     * 回复后状态默认为 resolved（已处理），或由请求指定
     */
    @PutMapping("/admin/reply")
    public Result<Void> reply(@Valid @RequestBody FeedbackReplyDto dto) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        feedbackService.replyFeedback(dto, ctx.getUserId().intValue());
        return Result.success(null);
    }

    /**
     * 管理员删除反馈（逻辑删除）
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        feedbackService.deleteFeedback(id, ctx.getUserId().intValue());
        return Result.success(null);
    }
}