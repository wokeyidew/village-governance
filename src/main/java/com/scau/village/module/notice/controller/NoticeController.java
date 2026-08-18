package com.scau.village.module.notice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.notice.entity.Notice;
import com.scau.village.module.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 村务通知控制器
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 分页获取通知列表（公开）
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;
        Page<Notice> pageParam = new Page<>(page, size);
        Page<Notice> result = noticeService.lambdaQuery()
                .eq(Notice::getTenantId, tenantId)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime)
                .page(pageParam);
        return Result.success(result);
    }

    /**
     * 发布通知（仅村委管理员）
     */
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody Notice notice) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        notice.setTenantId(ctx.getTenantId());
        notice.setCreateTime(LocalDateTime.now());
        noticeService.save(notice);
        return Result.success(null);
    }

    /**
     * 获取通知详情（自动增加阅读次数）
     * 权限：允许已登录用户访问，若需公开可去掉认证限制
     */
    @GetMapping("/{id}")
    public Result<Notice> getNoticeDetail(@PathVariable Integer id) {
        Notice notice = noticeService.getNoticeDetail(id);
        return Result.success(notice);
    }

    /**
     * 删除通知（仅村委管理员，逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotice(@PathVariable Integer id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN");

        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error(404, "通知不存在");
        }
        // 逻辑删除（@TableLogic 自动处理）
        noticeService.removeById(id);
        log.info("通知已删除，id={}, adminId={}", id, ctx.getUserId());
        return Result.success(null);
    }
}