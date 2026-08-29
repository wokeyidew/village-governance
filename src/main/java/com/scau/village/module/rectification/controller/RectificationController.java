package com.scau.village.module.rectification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.rectification.dto.SubmitRectificationDto;
import com.scau.village.module.rectification.dto.ReviewRectificationDto;
import com.scau.village.module.rectification.entity.RectificationTask;
import com.scau.village.module.rectification.service.RectificationTaskService;
import com.scau.village.module.rectification.vo.RectificationTaskVO;
import com.scau.village.module.rectification.vo.RectificationDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 整改任务控制器
 * 提供村民端和管理端的整改任务相关接口
 *
 * @author system
 * @since 2026-08-19
 */
@Slf4j
@RestController
@RequestMapping("/api/rectification")
@RequiredArgsConstructor
public class RectificationController {

    private final RectificationTaskService rectificationTaskService;

    // ==================== 村民端接口 ====================

    /**
     * 获取当前用户的整改任务列表（村民端）
     * 可按状态筛选
     *
     * @param status 任务状态（pending/reviewing/resolved/overdue），可选
     * @param page   页码，默认1
     * @param size   每页数量，默认10
     * @return 分页整改任务列表
     */
    @GetMapping("/my-tasks")
    public Result<Page<RectificationTaskVO>> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        Page<RectificationTaskVO> result = rectificationTaskService.getMyTasks(userId, status, page, size);
        return Result.success(result);
    }

    /**
     * 获取当前用户各状态的任务数量统计（村民端）
     * 用于在整改列表页显示各Tab的角标
     *
     * @return 各状态任务数量列表
     */
    @GetMapping("/my-task-counts")
    public Result<List<RectificationTaskVO>> getMyTaskCounts() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        List<RectificationTaskVO> counts = rectificationTaskService.getMyTaskCounts(userId);
        return Result.success(counts);
    }

    /**
     * 获取整改任务详情（村民端）
     * 
     * 修复说明：
     * - 参数类型为 String，直接传递给 Service 层，由 Service 层处理字符串 ID 查询
     * - 移除 Long.parseLong 转换，避免潜在的 NumberFormatException
     *
     * @param taskId 整改任务ID（字符串形式，由雪花算法生成）
     * @return 整改任务详情
     */
    @GetMapping("/task/{taskId}")
    public Result<RectificationDetailVO> getTaskDetail(@PathVariable String taskId) {
        log.info("【整改详情-Controller】村民端查询整改任务详情，taskId={}", taskId);

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【整改详情-Controller】用户未登录，taskId={}", taskId);
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();

        try {
            log.info("【整改详情-Controller】当前用户ID={}, taskId={}", userId, taskId);
            RectificationDetailVO detail = rectificationTaskService.getTaskDetail(taskId, userId);
            log.info("【整改详情-Controller】查询成功，taskId={}, userName={}",
                    taskId, detail != null ? detail.getUserName() : "null");
            return Result.success(detail);
        } catch (Exception e) {
            log.error("【整改详情-Controller】查询整改任务详情失败，taskId={}, userId={}, error={}",
                    taskId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 村民提交整改
     * 
     * 修复说明：
     * - SubmitRectificationDto 中的 taskId 为 String 类型，直接传递给 Service 层
     * - 移除 Long.parseLong 转换
     *
     * @param dto 提交整改请求体
     * @return 操作结果
     */
    @PostMapping("/submit")
    public Result<Void> submitRectification(@Valid @RequestBody SubmitRectificationDto dto) {
        log.info("【提交整改】收到提交整改请求，taskId={}", dto.getTaskId());

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【提交整改】用户未登录");
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();

        log.info("【提交整改】用户ID={}, taskId={}, 整改说明={}", userId, dto.getTaskId(), dto.getSubmitRemark());

        rectificationTaskService.submitRectification(
                dto.getTaskId(),
                userId,
                dto.getAfterPhotos(),
                dto.getSubmitRemark()
        );

        log.info("【提交整改】整改提交成功，taskId={}, userId={}", dto.getTaskId(), userId);
        return Result.success(null);
    }

    /**
     * 获取当前用户待整改任务数量（用于角标提示）
     *
     * @return 待整改任务数量
     */
    @GetMapping("/pending-count")
    public Result<Long> getPendingCount() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        Long count = rectificationTaskService.countPendingTasks(userId);
        return Result.success(count);
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员获取整改任务列表
     *
     * @param status 任务状态（pending/reviewing/resolved/overdue），可选
     * @param page   页码，默认1
     * @param size   每页数量，默认10
     * @return 分页整改任务列表
     */
    @GetMapping("/admin/list")
    public Result<Page<RectificationTaskVO>> getAdminTaskList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        Page<RectificationTaskVO> result = rectificationTaskService.getAdminTaskList(tenantId, status, page, size);
        return Result.success(result);
    }

    /**
     * 管理员获取整改任务详情
     * 
     * 修复说明：
     * - 参数类型为 String，直接传递给 Service 层，由 Service 层处理字符串 ID 查询
     * - 移除 Long.parseLong 转换
     *
     * @param taskId 整改任务ID（字符串形式，由雪花算法生成）
     * @return 整改任务详情
     */
    @GetMapping("/admin/task/{taskId}")
    public Result<RectificationDetailVO> getAdminTaskDetail(@PathVariable String taskId) {
        log.info("【整改详情-Controller】管理员端查询整改任务详情，taskId={}", taskId);

        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");

        try {
            log.info("【整改详情-Controller】管理员端查询，taskId={}", taskId);
            RectificationDetailVO detail = rectificationTaskService.getAdminTaskDetail(taskId);
            log.info("【整改详情-Controller】管理员端查询成功，taskId={}, userName={}",
                    taskId, detail != null ? detail.getUserName() : "null");
            return Result.success(detail);
        } catch (Exception e) {
            log.error("【整改详情-Controller】管理员端查询整改任务详情失败，taskId={}, error={}",
                    taskId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 管理员复核整改任务
     * 
     * 修复说明：
     * - ReviewRectificationDto 中的 taskId 为 String 类型，直接传递给 Service 层
     * - 移除 Long.parseLong 转换
     *
     * @param dto 复核请求体
     * @return 操作结果
     */
    @PostMapping("/admin/review")
    public Result<Void> reviewTask(@Valid @RequestBody ReviewRectificationDto dto) {
        log.info("【复核整改】收到复核请求，taskId={}, result={}", dto.getTaskId(), dto.getReviewResult());

        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【复核整改】用户未登录");
            return Result.error(401, "请先登录");
        }
        Long reviewerId = ctx.getUserId();

        log.info("【复核整改】复核人ID={}, taskId={}, result={}, remark={}",
                reviewerId, dto.getTaskId(), dto.getReviewResult(), dto.getReviewRemark());

        rectificationTaskService.reviewTask(
                dto.getTaskId(),
                reviewerId,
                dto.getReviewResult(),
                dto.getReviewRemark()
        );

        log.info("【复核整改】复核完成，taskId={}, reviewerId={}", dto.getTaskId(), reviewerId);
        return Result.success(null);
    }

    /**
     * 管理员根据批次ID获取整改任务列表
     * 
     * 修复说明：
     * - 参数类型为 String，直接传递给 Service 层
     * - 移除 Long.parseLong 转换
     *
     * @param batchId 批次ID（字符串形式，由雪花算法生成）
     * @param status  任务状态（可选）
     * @return 整改任务列表
     */
    @GetMapping("/admin/batch/{batchId}")
    public Result<List<RectificationTask>> getTasksByBatchId(
            @PathVariable String batchId,
            @RequestParam(required = false) String status) {
        log.info("【批次任务列表】查询批次ID={}, status={}", batchId, status);

        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");

        List<RectificationTask> tasks = rectificationTaskService.getTasksByBatchId(batchId, status);
        log.info("【批次任务列表】查询成功，批次ID={}, 任务数={}", batchId, tasks != null ? tasks.size() : 0);
        return Result.success(tasks);
    }

    /**
     * 管理员统计某个批次的整改完成率
     * 
     * 修复说明：
     * - 参数类型为 String，直接传递给 Service 层
     * - 移除 Long.parseLong 转换
     *
     * @param batchId 批次ID（字符串形式，由雪花算法生成）
     * @return 完成率（0-100之间的整数）
     */
    @GetMapping("/admin/completion-rate/{batchId}")
    public Result<Integer> getCompletionRate(@PathVariable String batchId) {
        log.info("【完成率统计】批次ID={}", batchId);

        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");

        Integer rate = rectificationTaskService.calculateCompletionRate(batchId);
        log.info("【完成率统计】批次ID={}, 完成率={}%", batchId, rate);
        return Result.success(rate);
    }
}