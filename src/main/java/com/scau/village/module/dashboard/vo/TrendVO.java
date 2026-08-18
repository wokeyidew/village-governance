package com.scau.village.module.dashboard.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 治理驾驶舱 - 趋势数据视图对象
 * 用于展示问题发现趋势（近6个月）
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class TrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 月份，格式：yyyy-MM
     * 例如：2026-08
     */
    private String month;

    /**
     * 该月问题总数（扣分项总数）
     */
    private Integer totalProblems;

    /**
     * 该月已整改问题数
     */
    private Integer resolvedProblems;

    /**
     * 该月整改完成率（百分比）
     * 计算方式：已整改问题数 / 问题总数 * 100
     */
    private Double completionRate;

    /**
     * 该月参与户数
     */
    private Integer participantCount;

    /**
     * 该月总扣分数（所有扣分项的绝对值之和）
     */
    private Integer totalPenaltyPoints;

    /**
     * 该月总加分数
     */
    private Integer totalBonusPoints;

    /**
     * 该月净得分（加分 - 扣分）
     */
    private Integer netPoints;

    // ==================== 便捷方法 ====================

    /**
     * 获取格式化的完成率（如 "78.5%"）
     */
    public String getCompletionRateText() {
        if (completionRate == null) {
            return "0%";
        }
        return String.format("%.1f%%", completionRate);
    }
}