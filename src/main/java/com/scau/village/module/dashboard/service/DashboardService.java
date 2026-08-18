package com.scau.village.module.dashboard.service;

import com.scau.village.module.dashboard.vo.DistributionVO;
import com.scau.village.module.dashboard.vo.EffectivenessVO;
import com.scau.village.module.dashboard.vo.ProblemTypeVO;
import com.scau.village.module.dashboard.vo.TrendVO;

import java.util.List;

/**
 * 治理驾驶舱服务接口
 * 提供趋势、分布、问题类型、效果等统计数据
 *
 * @author system
 * @since 2026-08-19
 */
public interface DashboardService {

    /**
     * 获取近6个月的问题发现趋势
     *
     * @param tenantId 租户ID
     * @return 趋势数据列表
     */
    List<TrendVO> getTrendData(Integer tenantId);

    /**
     * 获取各村组的平均分分布
     *
     * @param tenantId 租户ID
     * @return 分布数据列表
     */
    List<DistributionVO> getDistributionData(Integer tenantId);

    /**
     * 获取最常见的扣分类型Top5
     *
     * @param tenantId 租户ID
     * @return 问题类型列表
     */
    List<ProblemTypeVO> getProblemTypeTop5(Integer tenantId);

    /**
     * 获取治理效果综合指标
     *
     * @param tenantId 租户ID
     * @return 效果数据
     */
    EffectivenessVO getEffectivenessData(Integer tenantId);
}