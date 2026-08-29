package com.scau.village.module.points.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.DesensitizationUtils;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.points.dto.AdminUserPointsVO;
import com.scau.village.module.points.dto.ApplyDto;
import com.scau.village.module.points.dto.EvidenceVO;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.entity.ScoreEvidence;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.service.PointsApplyService;
import com.scau.village.module.points.service.PointsFlowService;
import com.scau.village.module.points.service.PointsRuleService;
import com.scau.village.module.points.service.ScoreEvidenceService;
import com.scau.village.module.points.utils.TemplateLoader;
import com.scau.village.module.points.vo.PointsFlowVO;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsRuleService ruleService;
    private final PointsApplyService applyService;
    private final PointsFlowService flowService;
    private final UserService userService;
    private final PointsApplyMapper pointsApplyMapper;
    private final ScoreEvidenceService scoreEvidenceService;

    // ==================== 村民端接口 ====================

    // 村民端：获取当前租户的积分规则列表（仅启用状态的规则）
    @GetMapping("/rules")
    public Result<?> listRules() {
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;
        return Result.success(ruleService.listByTenantId(tenantId));
    }

    // 村民提交申报
    @PostMapping("/apply")
    public Result<Void> submitApply(@Valid @RequestBody ApplyDto dto) {
        Long userId = UserContext.get().getUserId();
        Integer tenantId = UserContext.get().getTenantId();
        applyService.submitApply(dto, userId, tenantId);
        return Result.success(null);
    }

    // 村委审核申报
    @PutMapping("/apply/{applyId}/audit")
    public Result<Void> audit(@PathVariable Integer applyId,
                              @RequestParam Boolean approved,
                              @RequestParam(required = false) String remark) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        Long auditorId = UserContext.get().getUserId();
        applyService.approve(applyId, auditorId, approved, remark);
        return Result.success(null);
    }

    // ========== 管理员审核列表 ==========
    @GetMapping("/apply/list")
    public Result<Page<PointsApply>> listApply(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER", "TOWN_ADMIN");
        UserContext ctx = UserContext.get();
        Integer tenantId = ctx.getTenantId();
        Page<PointsApply> pageParam = new Page<>(page, size);
        Page<PointsApply> result = applyService.lambdaQuery()
                .eq(PointsApply::getTenantId, tenantId)
                .eq(status != null && !status.isEmpty(), PointsApply::getStatus, status)
                .orderByDesc(PointsApply::getCreateTime)
                .page(pageParam);
        return Result.success(result);
    }

    // ========== 积分流水（村民查看） ==========
    @GetMapping("/flow")
    public Result<Page<PointsFlowVO>> flow(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.get().getUserId();
        // 调用 Service 方法获取含批次信息的流水数据
        Page<PointsFlowVO> voPage = flowService.getFlowPage(userId.intValue(), page, size);
        return Result.success(voPage);
    }

    // 查询我的总积分
    @GetMapping("/myPoints")
    public Result<Integer> myPoints() {
        Long userId = UserContext.get().getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user.getPoints());
    }

    // ==================== 管理员端接口（新增） ====================

    /**
     * 管理员根据用户ID查询用户积分信息
     * 权限：仅 VILLAGE_ADMIN 或 SUPER_ADMIN
     */
    @GetMapping("/admin/user/{userId}/points")
    public Result<AdminUserPointsVO> getUserPointsByAdmin(@PathVariable Integer userId) {
        // 检查管理员权限
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        AdminUserPointsVO vo = new AdminUserPointsVO();
        vo.setUserId(user.getId());
        vo.setRealName(user.getRealName());
        vo.setPhone(DesensitizationUtils.mobile(user.getPhone()));
        vo.setPoints(user.getPoints());
        vo.setVillageGroup(user.getVillageGroup());
        vo.setRole(user.getRole());

        return Result.success(vo);
    }

    /**
     * 管理员根据手机号查询用户积分信息
     * 权限：仅 VILLAGE_ADMIN 或 SUPER_ADMIN
     */
    @GetMapping("/admin/user/points")
    public Result<AdminUserPointsVO> getUserPointsByPhone(@RequestParam String phone) {
        // 检查管理员权限
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userService.getOne(wrapper);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        AdminUserPointsVO vo = new AdminUserPointsVO();
        vo.setUserId(user.getId());
        vo.setRealName(user.getRealName());
        vo.setPhone(DesensitizationUtils.mobile(user.getPhone()));
        vo.setPoints(user.getPoints());
        vo.setVillageGroup(user.getVillageGroup());
        vo.setRole(user.getRole());

        return Result.success(vo);
    }

    // ==================== 积分规则管理（村委/镇管理员专用） ====================

    @GetMapping("/rules/list")
    public Result<List<PointsRule>> getRulesList() {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "TOWN_ADMIN");
        Integer tenantId = UserContext.get().getTenantId();
        return Result.success(ruleService.lambdaQuery()
                .eq(PointsRule::getTenantId, tenantId)
                .orderByAsc(PointsRule::getSortOrder)
                .list());
    }

    @GetMapping("/rules/{id}")
    public Result<PointsRule> getRuleById(@PathVariable Integer id) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "TOWN_ADMIN");
        PointsRule rule = ruleService.getById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }

    @PostMapping("/rules")
    public Result<Void> addRule(@Valid @RequestBody PointsRule rule) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "TOWN_ADMIN");
        Integer tenantId = UserContext.get().getTenantId();
        rule.setId(null);
        rule.setTenantId(tenantId);
        ruleService.save(rule);
        return Result.success(null);
    }

    @PutMapping("/rules/{id}")
    public Result<Void> updateRule(@PathVariable Integer id, @Valid @RequestBody PointsRule rule) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "TOWN_ADMIN");
        rule.setId(id);
        rule.setTenantId(null);
        ruleService.updateById(rule);
        return Result.success(null);
    }

    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Integer id) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        ruleService.removeById(id);
        return Result.success(null);
    }

    // ==================== 证据查询接口 ====================

    /**
     * 查询积分记录的证据详情
     * 村民只能查看自己的积分记录证据；管理员可查看所有。
     *
     * 修复说明（2026-08-30）：
     * - applyId 路径参数从 Long 改为 String，避免前端雪花ID精度丢失
     * - 调用 applyService.getById(applyId) 直接使用 String
     * - 调用 scoreEvidenceService.getByApplyId(applyId) 直接使用 String
     *
     * @param applyId 积分申请/评分记录ID（points_apply.id，雪花ID字符串）
     * @return 证据详情
     */
    @GetMapping("/evidence/{applyId}")
    public Result<EvidenceVO> getEvidence(@PathVariable String applyId) {
        // 增加日志：记录入参
        log.info("【证据查询】收到请求，applyId={}", applyId);

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【证据查询】用户未登录，applyId={}", applyId);
            return Result.error(401, "请先登录");
        }

        // 1. 查询积分记录
        PointsApply apply = applyService.getById(applyId);
        if (apply == null) {
            log.warn("【证据查询】积分记录不存在，applyId={}", applyId);
            return Result.error(404, "积分记录不存在");
        }

        log.info("【证据查询】积分记录存在，applyId={}, userId={}, hasEvidence={}",
                applyId, apply.getUserId(), apply.getHasEvidence());

        // 2. 权限校验：村民只能查看自己的记录，管理员可查看所有
        Long currentUserId = ctx.getUserId();
        String role = ctx.getRole();
        boolean isAdmin = "VILLAGE_ADMIN".equals(role) || "GRID_MEMBER".equals(role) || "SUPER_ADMIN".equals(role);
        if (!isAdmin && !apply.getUserId().equals(currentUserId.intValue())) {
            log.warn("【证据查询】权限校验失败，applyId={}, 记录所属用户={}, 当前用户={}",
                    applyId, apply.getUserId(), currentUserId);
            return Result.error(403, "无权查看此记录的证据");
        }

        // 3. 查询证据
        ScoreEvidence evidence = scoreEvidenceService.getByApplyId(applyId);
        if (evidence == null) {
            // 如果没有证据，返回空数据（前端可根据 hasEvidence 字段判断）
            log.info("【证据查询】未找到证据，applyId={}", applyId);
            EvidenceVO emptyVo = new EvidenceVO();
            emptyVo.setHasEvidence(false);
            return Result.success(emptyVo);
        }

        log.info("【证据查询】证据存在，applyId={}, evidenceId={}, photoUrls={}",
                applyId, evidence.getId(), evidence.getPhotoUrls());

        // 4. 构建返回VO
        EvidenceVO vo = new EvidenceVO();
        vo.setHasEvidence(true);
        vo.setPhotoUrls(evidence.getPhotoUrls() != null ? evidence.getPhotoUrls().split(",") : new String[0]);
        vo.setPhotoCount(evidence.getPhotoCount());
        vo.setLocation(evidence.getLocation());
        vo.setInspectorId(evidence.getInspectorId());
        vo.setBatchId(evidence.getBatchId());
        vo.setRuleVersion(evidence.getRuleVersion());
        vo.setHasWatermark(evidence.getHasWatermark() != null && evidence.getHasWatermark() == 1);
        vo.setRuleName(evidence.getRuleName());
        vo.setUserName(evidence.getUserName());
        vo.setCreateTime(evidence.getCreateTime());

        log.info("【证据查询】证据返回成功，applyId={}", applyId);
        return Result.success(vo);
    }
}