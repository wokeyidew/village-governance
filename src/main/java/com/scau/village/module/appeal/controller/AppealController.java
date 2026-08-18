package com.scau.village.module.appeal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.appeal.dto.SubmitAppealDto;
import com.scau.village.module.appeal.dto.HandleAppealDto;
import com.scau.village.module.appeal.entity.AppealRecord;
import com.scau.village.module.appeal.service.AppealRecordService;
import com.scau.village.module.appeal.vo.AppealVO;
import com.scau.village.module.appeal.vo.AppealDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 申诉记录控制器
 * 提供村民端和管理端的申诉相关接口
 *
 * @author system
 * @since 2026-08-19
 */
@Slf4j
@RestController
@RequestMapping("/api/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealRecordService appealRecordService;

    // ==================== 村民端接口 ====================

    /**
     * 村民提交申诉
     *
     * @param dto 提交申诉请求体
     * @return 操作结果
     */
    @PostMapping("/submit")
    public Result<Void> submitAppeal(@Valid @RequestBody SubmitAppealDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        if (ctx.getTenantId() == null) {
            return Result.error(400, "租户信息缺失，请重新登录");
        }
        Long userId = ctx.getUserId();
        Integer tenantId = ctx.getTenantId();

        appealRecordService.submitAppeal(
                dto.getApplyId(),
                userId,
                dto.getReason(),
                dto.getEvidencePhotos(),
                tenantId,
                dto.getBatchId()
        );
        return Result.success(null);
    }

    /**
     * 获取当前用户的申诉列表（村民端）
     * 可按状态筛选
     *
     * @param status 申诉状态（pending/resolved），可选
     * @param page   页码，默认1
     * @param size   每页数量，默认10
     * @return 分页申诉列表
     */
    @GetMapping("/my-list")
    public Result<Page<AppealVO>> getMyAppeals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        Page<AppealVO> result = appealRecordService.getMyAppeals(userId, status, page, size);
        return Result.success(result);
    }

    /**
     * 获取申诉详情（村民端）
     *
     * @param appealId 申诉记录ID
     * @return 申诉详情
     */
    @GetMapping("/detail/{appealId}")
    public Result<AppealDetailVO> getAppealDetail(@PathVariable Long appealId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        AppealDetailVO detail = appealRecordService.getAppealDetail(appealId, userId);
        return Result.success(detail);
    }

    /**
     * 获取当前用户各状态的申诉数量统计（村民端）
     *
     * @return 各状态申诉数量
     */
    @GetMapping("/my-counts")
    public Result<List<AppealVO>> getMyAppealCounts() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        List<AppealVO> counts = appealRecordService.getMyAppealCounts(userId);
        return Result.success(counts);
    }

    // ==================== 管理员端接口 ====================

    /**
     * 管理员获取所有申诉列表
     *
     * @param status 申诉状态（pending/resolved），可选
     * @param page   页码，默认1
     * @param size   每页数量，默认10
     * @return 分页申诉列表
     */
    @GetMapping("/admin/list")
    public Result<Page<AppealVO>> getAdminAppeals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        Page<AppealVO> result = appealRecordService.getAdminAppeals(tenantId, status, page, size);
        return Result.success(result);
    }

    /**
     * 管理员获取申诉详情
     *
     * @param appealId 申诉记录ID
     * @return 申诉详情
     */
    @GetMapping("/admin/detail/{appealId}")
    public Result<AppealDetailVO> getAdminAppealDetail(@PathVariable Long appealId) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        AppealDetailVO detail = appealRecordService.getAdminAppealDetail(appealId);
        return Result.success(detail);
    }

    /**
     * 管理员获取所有待处理的申诉列表
     *
     * @return 待处理申诉列表
     */
    @GetMapping("/admin/pending-list")
    public Result<List<AppealRecord>> getPendingAppeals() {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        List<AppealRecord> list = appealRecordService.getPendingAppeals(tenantId);
        return Result.success(list);
    }

    /**
     * 管理员处理申诉
     *
     * @param dto 处理申诉请求体
     * @return 操作结果
     */
    @PostMapping("/admin/handle")
    public Result<Void> handleAppeal(@Valid @RequestBody HandleAppealDto dto) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long reviewerId = ctx.getUserId();

        appealRecordService.handleAppeal(
                dto.getAppealId(),
                reviewerId,
                dto.getDecision(),
                dto.getDecisionDetail(),
                dto.getNewPoints()
        );
        return Result.success(null);
    }

    /**
     * 检查某条积分记录是否已存在待处理的申诉
     *
     * @param applyId 积分申请记录ID
     * @return true-存在待处理申诉，false-不存在
     */
    @GetMapping("/check-pending")
    public Result<Boolean> checkPendingAppeal(@RequestParam Long applyId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        boolean hasPending = appealRecordService.hasPendingAppeal(applyId);
        return Result.success(hasPending);
    }
}