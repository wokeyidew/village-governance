package com.scau.village.module.points.controller;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.points.entity.QuarterlySnapshot;
import com.scau.village.module.points.service.QuarterlySnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 季度榜单控制器
 * 提供红榜、蜕变榜、帮扶榜的查询接口，以及季度结算管理功能
 * 路径前缀：/api/quarterly
 *
 * 榜单说明：
 * - 红榜（red）：本季度积分排名前10%的村民，公开可见
 * - 蜕变榜（progress）：本季度进步最大的前10%村民，公开可见
 * - 帮扶榜（warning）：本季度净积分为负的村民，仅管理员可见
 *
 * @author system
 * @since 2026-08-28
 */
@Slf4j
@RestController
@RequestMapping("/api/quarterly")
@RequiredArgsConstructor
public class QuarterlyController {

    private final QuarterlySnapshotService quarterlySnapshotService;

    // ==================== 公开接口（无需认证） ====================

    /**
     * 获取红榜列表（公开接口）
     * 红榜标准：tag = 'red'，按 rank_points 升序排列
     *
     * @param quarter 季度标识，如：2026-Q3（可选，默认当前季度）
     * @return 红榜列表
     */
    @GetMapping("/red-list")
    public Result<List<QuarterlySnapshot>> getRedList(
            @RequestParam(required = false) String quarter) {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }
        log.info("【红榜查询】tenantId={}, quarter={}", tenantId, quarter);

        List<QuarterlySnapshot> list = quarterlySnapshotService.getRedList(tenantId, quarter);
        return Result.success(list);
    }

    /**
     * 获取红榜列表（带分页，公开接口）
     *
     * @param quarter  季度标识（可选，默认当前季度）
     * @param pageNum  页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页红榜列表
     */
    @GetMapping("/red-list/page")
    public Result<Map<String, Object>> getRedListPage(
            @RequestParam(required = false) String quarter,
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize) {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }

        List<QuarterlySnapshot> list = quarterlySnapshotService.getRedList(tenantId, quarter, pageNum, pageSize);
        Integer total = quarterlySnapshotService.countRedList(tenantId, quarter);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    /**
     * 获取蜕变榜列表（公开接口）
     * 蜕变榜标准：tag = 'progress'，按 rank_progress 升序排列
     *
     * @param quarter 季度标识（可选，默认当前季度）
     * @return 蜕变榜列表
     */
    @GetMapping("/progress-list")
    public Result<List<QuarterlySnapshot>> getProgressList(
            @RequestParam(required = false) String quarter) {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }
        log.info("【蜕变榜查询】tenantId={}, quarter={}", tenantId, quarter);

        List<QuarterlySnapshot> list = quarterlySnapshotService.getProgressList(tenantId, quarter);
        return Result.success(list);
    }

    /**
     * 获取蜕变榜列表（带分页，公开接口）
     *
     * @param quarter  季度标识（可选，默认当前季度）
     * @param pageNum  页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页蜕变榜列表
     */
    @GetMapping("/progress-list/page")
    public Result<Map<String, Object>> getProgressListPage(
            @RequestParam(required = false) String quarter,
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize) {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }

        List<QuarterlySnapshot> list = quarterlySnapshotService.getProgressList(tenantId, quarter, pageNum, pageSize);
        Integer total = quarterlySnapshotService.countProgressList(tenantId, quarter);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    // ==================== 需认证接口 ====================

    /**
     * 获取个人季度数据（村民端）
     * 返回当前登录用户指定季度的快照数据
     *
     * @param quarter 季度标识（可选，默认当前季度）
     * @return 个人季度快照数据
     */
    @GetMapping("/my-data")
    public Result<QuarterlySnapshot> getMyQuarterData(
            @RequestParam(required = false) String quarter) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【个人季度数据】用户未登录");
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        Integer userId = ctx.getUserId().intValue();
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }
        log.info("【个人季度数据】userId={}, quarter={}", userId, quarter);

        QuarterlySnapshot data = quarterlySnapshotService.getUserQuarterDataOrDefault(tenantId, quarter, userId);
        return Result.success(data);
    }

    /**
     * 获取个人历史季度数据（村民端）
     * 返回当前登录用户所有季度的快照数据
     *
     * @return 历史季度快照列表
     */
    @GetMapping("/my-history")
    public Result<List<QuarterlySnapshot>> getMyHistory() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【个人历史数据】用户未登录");
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        Integer userId = ctx.getUserId().intValue();

        List<QuarterlySnapshot> history = quarterlySnapshotService.getUserHistory(tenantId, userId);
        return Result.success(history);
    }

    /**
     * 获取个人在指定季度的排名（村民端）
     *
     * @param quarter 季度标识（可选，默认当前季度）
     * @return 排名信息（含排名数字和标签）
     */
    @GetMapping("/my-rank")
    public Result<Map<String, Object>> getMyRank(
            @RequestParam(required = false) String quarter) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("【个人排名】用户未登录");
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        Integer userId = ctx.getUserId().intValue();
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }

        QuarterlySnapshot data = quarterlySnapshotService.getUserQuarterData(tenantId, quarter, userId);
        Map<String, Object> result = new HashMap<>();
        if (data != null) {
            result.put("rank", data.getRankPoints());
            result.put("tag", data.getTag());
            result.put("quarterEarnedPoints", data.getQuarterEarnedPoints());
            result.put("progressPoints", data.getProgressPoints());
        } else {
            result.put("rank", null);
            result.put("tag", null);
            result.put("quarterEarnedPoints", 0);
            result.put("progressPoints", 0);
        }
        return Result.success(result);
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取帮扶榜列表（管理员后台）
     * 帮扶榜标准：tag = 'warning'，即净积分为负的家庭
     *
     * @param quarter 季度标识（可选，默认当前季度）
     * @return 帮扶榜列表
     */
    @GetMapping("/admin/warning-list")
    public Result<List<QuarterlySnapshot>> getWarningList(
            @RequestParam(required = false) String quarter) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER", "SUPER_ADMIN");
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }
        log.info("【帮扶榜查询-管理员】tenantId={}, quarter={}", tenantId, quarter);

        List<QuarterlySnapshot> list = quarterlySnapshotService.getWarningList(tenantId, quarter);
        return Result.success(list);
    }

    /**
     * 获取帮扶榜列表（带分页，管理员后台）
     *
     * @param quarter  季度标识（可选，默认当前季度）
     * @param pageNum  页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页帮扶榜列表
     */
    @GetMapping("/admin/warning-list/page")
    public Result<Map<String, Object>> getWarningListPage(
            @RequestParam(required = false) String quarter,
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER", "SUPER_ADMIN");
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }

        List<QuarterlySnapshot> allList = quarterlySnapshotService.getWarningList(tenantId, quarter);
        int total = allList.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<QuarterlySnapshot> list = start < total ? allList.subList(start, end) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    /**
     * 获取榜单统计信息（管理员后台）
     * 返回红榜、蜕变榜、帮扶榜的人数统计
     *
     * @param quarter 季度标识（可选，默认当前季度）
     * @return 榜单统计信息
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Integer>> getStatistics(
            @RequestParam(required = false) String quarter) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER", "SUPER_ADMIN");
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        if (quarter == null) {
            quarter = quarterlySnapshotService.getCurrentQuarter();
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("redCount", quarterlySnapshotService.countRedList(tenantId, quarter));
        stats.put("progressCount", quarterlySnapshotService.countProgressList(tenantId, quarter));
        stats.put("warningCount", quarterlySnapshotService.countWarningList(tenantId, quarter));

        return Result.success(stats);
    }

    /**
     * 获取可用季度列表（管理员后台）
     * 用于前端下拉选择器
     *
     * @return 季度标识列表，按时间降序排列
     */
    @GetMapping("/admin/available-quarters")
    public Result<List<String>> getAvailableQuarters() {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER", "SUPER_ADMIN");
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        List<String> quarters = quarterlySnapshotService.getAvailableQuarters(tenantId);
        return Result.success(quarters);
    }

    /**
     * 手动触发季度结算（管理员专用）
     * 用于补录或重新计算某季度数据
     *
     * @param quarter 季度标识，如：2026-Q3
     * @param force   是否强制重新计算（true：删除已有数据重新计算）
     * @return 操作结果
     */
    @PostMapping("/admin/settle")
    public Result<Void> settleQuarter(
            @RequestParam @NotBlank(message = "季度标识不能为空") String quarter,
            @RequestParam(defaultValue = "false") Boolean force) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        log.info("【手动结算】管理员触发季度结算，quarter={}, force={}, tenantId={}",
                quarter, force, tenantId);

        try {
            quarterlySnapshotService.settleQuarterManual(quarter, tenantId, force);
            log.info("【手动结算】季度结算成功，quarter={}", quarter);
            return Result.success(null);
        } catch (Exception e) {
            log.error("【手动结算】季度结算失败，quarter={}", quarter, e);
            return Result.error(500, "季度结算失败: " + e.getMessage());
        }
    }

    /**
     * 删除某季度数据（管理员专用）
     * 用于季度数据异常时重置
     *
     * @param quarter 季度标识，如：2026-Q3
     * @return 删除记录数
     */
    @DeleteMapping("/admin/delete")
    public Result<Integer> deleteQuarterData(
            @RequestParam @NotBlank(message = "季度标识不能为空") String quarter) {
        SecurityUtils.checkRole("VILLAGE_ADMIN", "SUPER_ADMIN");
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        log.info("【删除季度数据】管理员删除季度数据，quarter={}, tenantId={}", quarter, tenantId);

        Integer deletedCount = quarterlySnapshotService.deleteQuarterData(quarter, tenantId);
        log.info("【删除季度数据】删除成功，quarter={}, deletedCount={}", quarter, deletedCount);
        return Result.success(deletedCount);
    }

    /**
     * 获取当前季度标识（工具接口）
     *
     * @return 当前季度标识，如：2026-Q3
     */
    @GetMapping("/current-quarter")
    public Result<String> getCurrentQuarter() {
        String quarter = quarterlySnapshotService.getCurrentQuarter();
        return Result.success(quarter);
    }

}