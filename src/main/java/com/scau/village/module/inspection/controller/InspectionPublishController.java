package com.scau.village.module.inspection.controller;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.inspection.dto.PublishResultDto;
import com.scau.village.module.inspection.entity.InspectionPublish;
import com.scau.village.module.inspection.service.InspectionPublishService;
import com.scau.village.module.inspection.vo.InspectionResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 评比结果发布控制器
 * 提供评比结果的发布、取消发布、查看等功能
 *
 * @author system
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
public class InspectionPublishController {

    private final InspectionPublishService publishService;

    /**
     * 发布评比结果（管理员）
     * 权限：VILLAGE_ADMIN
     *
     * @param dto 发布请求（包含批次ID和备注）
     * @return 空
     */
    @PostMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody PublishResultDto dto) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        Integer publisherId = ctx.getUserId().intValue();
        publishService.publish(dto.getBatchId(), tenantId, publisherId, dto.getRemark());
        return Result.success(null);
    }

    /**
     * 取消发布评比结果（管理员）
     * 权限：VILLAGE_ADMIN
     *
     * @param batchId 批次ID
     * @return 空
     */
    @DeleteMapping("/unpublish/{batchId}")
    public Result<Void> unpublish(@PathVariable Long batchId) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        boolean success = publishService.unpublish(batchId, tenantId);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error(500, "取消发布失败");
        }
    }

    /**
     * 检查批次是否已发布（管理员或村民均可）
     *
     * @param batchId 批次ID
     * @return true-已发布，false-未发布
     */
    @GetMapping("/is-published/{batchId}")
    public Result<Boolean> isPublished(@PathVariable Long batchId) {
        boolean published = publishService.isPublished(batchId);
        return Result.success(published);
    }

    /**
     * 村民端获取指定批次的评比结果（仅当已发布）
     *
     * @param batchId 批次ID
     * @return 当前用户的评比结果明细
     */
    @GetMapping("/result/{batchId}")
    public Result<InspectionResultVO> getResultForUser(@PathVariable Long batchId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer userId = ctx.getUserId().intValue();
        InspectionResultVO vo = publishService.getResultForUser(batchId, userId);
        return Result.success(vo);
    }

    /**
     * 获取当前租户下所有已发布的批次列表（村民端查看有哪些批次可查）
     *
     * @return 已发布的批次列表（仅包含基本信息）
     */
    @GetMapping("/published-list")
    public Result<List<InspectionPublish>> getPublishedBatchList() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        List<InspectionPublish> list = publishService.getPublishedBatchList(tenantId);
        return Result.success(list);
    }

    /**
     * 管理员获取某个批次的完整发布快照（含所有用户数据）
     * 权限：VILLAGE_ADMIN
     *
     * @param batchId 批次ID
     * @return 发布记录（含完整快照JSON）
     */
    @GetMapping("/admin/publish-detail/{batchId}")
    public Result<InspectionPublish> getFullPublishDetail(@PathVariable Long batchId) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        InspectionPublish detail = publishService.getFullPublishDetail(batchId, tenantId);
        return Result.success(detail);
    }
}