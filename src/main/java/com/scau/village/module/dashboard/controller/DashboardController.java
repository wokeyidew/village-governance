package com.scau.village.module.dashboard.controller;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.module.dashboard.service.DashboardService;
import com.scau.village.module.dashboard.vo.DistributionVO;
import com.scau.village.module.dashboard.vo.EffectivenessVO;
import com.scau.village.module.dashboard.vo.ProblemTypeVO;
import com.scau.village.module.dashboard.vo.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 治理驾驶舱控制器
 * 提供趋势、分布、问题类型、效果等统计接口
 *
 * @author system
 * @since 2026-08-19
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 1. 获取近6个月的问题发现趋势
     * 返回折线图数据
     */
    @GetMapping("/trend")
    public Result<List<TrendVO>> getTrend() {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.error(401, "请先登录");
        }
        List<TrendVO> data = dashboardService.getTrendData(tenantId);
        return Result.success(data);
    }

    /**
     * 2. 获取各村组的平均分分布
     * 返回柱状图数据
     */
    @GetMapping("/distribution")
    public Result<List<DistributionVO>> getDistribution() {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.error(401, "请先登录");
        }
        List<DistributionVO> data = dashboardService.getDistributionData(tenantId);
        return Result.success(data);
    }

    /**
     * 3. 获取最常见的扣分类型Top5
     * 返回饼图数据
     */
    @GetMapping("/problem-types")
    public Result<List<ProblemTypeVO>> getProblemTypes() {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.error(401, "请先登录");
        }
        List<ProblemTypeVO> data = dashboardService.getProblemTypeTop5(tenantId);
        return Result.success(data);
    }

    /**
     * 4. 获取治理效果综合指标
     * 返回整改完成率、参与率、红黑榜增长率等核心指标
     */
    @GetMapping("/effectiveness")
    public Result<EffectivenessVO> getEffectiveness() {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.error(401, "请先登录");
        }
        EffectivenessVO data = dashboardService.getEffectivenessData(tenantId);
        return Result.success(data);
    }
}