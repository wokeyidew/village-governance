package com.scau.village.module.policy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.policy.entity.Policy;
import com.scau.village.module.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 政策推送控制器
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    /**
     * 分页获取政策列表（公开）
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String category) {
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;
        Page<Policy> pageParam = new Page<>(page, size);
        Page<Policy> result = policyService.lambdaQuery()
                .eq(Policy::getTenantId, tenantId)
                .eq(category != null && !category.isEmpty(), Policy::getCategory, category)
                .orderByDesc(Policy::getCreateTime)
                .page(pageParam);
        return Result.success(result);
    }

    /**
     * 发布政策（仅村委管理员）
     */
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody Policy policy) {
        log.info("收到发布政策请求: title={}, category={}", policy.getTitle(), policy.getCategory());

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("发布政策失败: 用户未登录");
            return Result.error(401, "请先登录");
        }

        SecurityUtils.checkRole("VILLAGE_ADMIN");

        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
            log.warn("当前用户 tenantId 为空，使用默认值 1");
        }
        policy.setTenantId(tenantId);
        policy.setCreateTime(LocalDateTime.now());

        policyService.save(policy);
        log.info("政策发布成功: id={}, title={}", policy.getId(), policy.getTitle());
        return Result.success(null);
    }

    /**
     * 获取政策详情
     * @param id 政策ID
     * @return 政策对象
     */
    @GetMapping("/detail/{id}")
    public Result<Policy> getPolicyDetail(@PathVariable Integer id) {
        Policy policy = policyService.getPolicyDetail(id);
        return Result.success(policy);
    }

    /**
     * 删除政策（仅村委管理员，逻辑删除）
     * @param id 政策ID
     * @return 空
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePolicy(@PathVariable Integer id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN");

        Policy policy = policyService.getById(id);
        if (policy == null) {
            return Result.error(404, "政策不存在");
        }
        policyService.removeById(id);
        log.info("政策已删除，id={}, adminId={}", id, ctx.getUserId());
        return Result.success(null);
    }
}