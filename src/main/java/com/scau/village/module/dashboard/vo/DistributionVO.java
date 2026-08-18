package com.scau.village.module.dashboard.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 治理驾驶舱 - 分布数据视图对象
 * 用于展示各村组的平均分对比（柱状图数据）
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class DistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 村组名称
     * 如：一区、二区、三区、移民新村
     */
    private String villageGroup;

    /**
     * 该村组平均分
     */
    private Double averageScore;

    /**
     * 该村组参与户数
     */
    private Integer participantCount;

    /**
     * 该村组最高分
     */
    private Integer maxScore;

    /**
     * 该村组最低分
     */
    private Integer minScore;

    /**
     * 该村组问题总数（扣分项总数）
     */
    private Integer problemCount;

    /**
     * 该村组已整改问题数
     */
    private Integer resolvedCount;

    /**
     * 该村组整改完成率（百分比）
     */
    private Double completionRate;

    /**
     * 该村组排名（按平均分降序）
     */
    private Integer rank;

    // ==================== 便捷方法 ====================

    /**
     * 获取平均分的格式化文本
     * 如 "85.5"
     */
    public String getAverageScoreText() {
        if (averageScore == null) {
            return "0.0";
        }
        return String.format("%.1f", averageScore);
    }

    /**
     * 获取完成率格式化文本
     */
    public String getCompletionRateText() {
        if (completionRate == null) {
            return "0%";
        }
        return String.format("%.1f%%", completionRate);
    }

    /**
     * 判断该村组是否高于平均水平（相对于所有村组的平均分，需在Service层计算后设置）
     * 此字段不在VO中，由前端自行判断或由Service层计算后添加到扩展字段
     */
    public boolean isAboveAverage(double overallAverage) {
        return averageScore != null && averageScore >= overallAverage;
    }
}