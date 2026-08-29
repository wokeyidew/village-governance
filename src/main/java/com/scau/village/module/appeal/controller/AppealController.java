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
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 申诉记录控制器
 * 提供村民端和管理端的申诉相关接口
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数从 Long 改为 String，Service 层方法参数同步改为 String
 * - 移除 Controller 中多余的 Long.parseLong() 转换，直接传递 String 给 Service
 * - 保留业务校验（扣分项校验）
 * - 修复 pointsApplyMapper.selectById 参数类型：将 String 转换为 Long
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
    private final PointsApplyMapper pointsApplyMapper;
    private final PointsRuleMapper pointsRuleMapper;

    // ==================== 村民端接口 ====================

    /**
     * 村民提交申诉
     * 
     * 修复说明：
     * - SubmitAppealDto 中的 applyId 和 batchId 为 String 类型，直接传递给 Service
     * - 增加扣分项校验：只有扣分记录才能申诉
     * - 将 applyId 转换为 Long 后再查询 points_apply 表
     *
     * @param dto 提交申诉请求体
     * @return 操作结果
     */
    @PostMapping("/submit")
    public Result<Void> submitAppeal(@Valid @RequestBody SubmitAppealDto dto) {
        log.info("【申诉提交】收到申诉请求，applyId={}, batchId={}, reason={}",
                dto.getApplyId(), dto.getBatchId(), dto.getReason());

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【申诉提交】用户未登录");
            return Result.error(401, "请先登录");
        }
        if (ctx.getTenantId() == null) {
            log.warn("【申诉提交】租户信息缺失");
            return Result.error(400, "租户信息缺失，请重新登录");
        }

        // 校验积分记录是否存在，并判断是否为扣分项
        Long applyIdLong;
        try {
            applyIdLong = Long.parseLong(dto.getApplyId());
        } catch (NumberFormatException e) {
            log.warn("【申诉提交】applyId 格式错误: {}", dto.getApplyId());
            return Result.error(400, "积分记录ID格式错误");
        }
        PointsApply apply = pointsApplyMapper.selectById(applyIdLong);
        if (apply == null) {
            log.warn("【申诉提交】积分记录不存在，applyId={}", dto.getApplyId());
            return Result.error(400, "积分记录不存在");
        }

        // 通过规则ID查询积分值，判断是加分还是扣分
        PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
        if (rule == null) {
            log.warn("【申诉提交】关联的积分规则不存在，ruleId={}", apply.getRuleId());
            return Result.error(400, "关联的积分规则不存在");
        }

        // 只有扣分项（points < 0）才能申诉
        if (rule.getPoints() >= 0) {
            log.warn("【申诉提交】加分记录不允许申诉，applyId={}, ruleId={}, points={}",
                    dto.getApplyId(), apply.getRuleId(), rule.getPoints());
            return Result.error(400, "只有扣分记录才能申诉");
        }

        Long userId = ctx.getUserId();
        Integer tenantId = ctx.getTenantId();

        // 直接传递 String 给 Service
        appealRecordService.submitAppeal(
                dto.getApplyId(),
                userId,
                dto.getReason(),
                dto.getEvidencePhotos(),
                tenantId,
                dto.getBatchId()
        );

        log.info("【申诉提交】申诉提交成功，applyId={}, userId={}", dto.getApplyId(), userId);
        return Result.success(null);
    }

    /**
     * 获取当前用户的申诉列表（村民端）
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
     * 修复说明：参数为 String 类型，直接传递给 Service
     *
     * @param appealId 申诉记录ID（字符串形式，由雪花算法生成）
     * @return 申诉详情
     */
    @GetMapping("/detail/{appealId}")
    public Result<AppealDetailVO> getAppealDetail(@PathVariable String appealId) {
        log.info("【申诉详情-村民端】查询申诉详情，appealId={}", appealId);

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【申诉详情-村民端】用户未登录");
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();

        AppealDetailVO detail = appealRecordService.getAppealDetail(appealId, userId);
        log.info("【申诉详情-村民端】查询成功，appealId={}", appealId);
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
     * 修复说明：参数为 String 类型，直接传递给 Service
     *
     * @param appealId 申诉记录ID（字符串形式，由雪花算法生成）
     * @return 申诉详情
     */
    @GetMapping("/admin/detail/{appealId}")
    public Result<AppealDetailVO> getAdminAppealDetail(@PathVariable String appealId) {
        log.info("【申诉详情-管理员端】查询申诉详情，appealId={}", appealId);

        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");

        AppealDetailVO detail = appealRecordService.getAdminAppealDetail(appealId);
        log.info("【申诉详情-管理员端】查询成功，appealId={}", appealId);
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
     * 修复说明：
     * - HandleAppealDto 中的 appealId 为 String 类型，直接传递给 Service
     *
     * @param dto 处理申诉请求体
     * @return 操作结果
     */
    @PostMapping("/admin/handle")
    public Result<Void> handleAppeal(@Valid @RequestBody HandleAppealDto dto) {
        log.info("【处理申诉】收到处理申诉请求，appealId={}, decision={}",
                dto.getAppealId(), dto.getDecision());

        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【处理申诉】用户未登录");
            return Result.error(401, "请先登录");
        }
        Long reviewerId = ctx.getUserId();

        log.info("【处理申诉】复核人ID={}, appealId={}, decision={}, detail={}, newPoints={}",
                reviewerId, dto.getAppealId(), dto.getDecision(), dto.getDecisionDetail(), dto.getNewPoints());

        appealRecordService.handleAppeal(
                dto.getAppealId(),
                reviewerId,
                dto.getDecision(),
                dto.getDecisionDetail(),
                dto.getNewPoints()
        );

        log.info("【处理申诉】申诉处理完成，appealId={}, reviewerId={}", dto.getAppealId(), reviewerId);
        return Result.success(null);
    }

    /**
     * 检查某条积分记录是否已存在待处理的申诉
     * 
     * 修复说明：参数为 String 类型，直接传递给 Service
     *
     * @param applyId 积分申请记录ID（字符串形式，由雪花算法生成）
     * @return true-存在待处理申诉，false-不存在
     */
    @GetMapping("/check-pending")
    public Result<Boolean> checkPendingAppeal(@RequestParam String applyId) {
        log.info("【检查待处理申诉】applyId={}", applyId);

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【检查待处理申诉】用户未登录");
            return Result.error(401, "请先登录");
        }

        boolean hasPending = appealRecordService.hasPendingAppeal(applyId);
        log.info("【检查待处理申诉】applyId={}, hasPending={}", applyId, hasPending);
        return Result.success(hasPending);
    }
}