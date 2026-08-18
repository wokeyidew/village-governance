package com.scau.village.module.dashboard.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 治理驾驶舱 - 效果数据视图对象
 * 用于展示治理效果的关键指标
 * 包括：整改完成率、参与率、红榜增长率等核心效果数据
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class EffectivenessVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 整改效率指标 ====================

    /**
     * 整改完成率（百分比）
     * 计算方式：已销项整改任务数 / 整改任务总数 * 100
     */
    private Double rectificationCompletionRate;

    /**
     * 整改任务总数
     */
    private Integer totalRectificationTasks;

    /**
     * 已销项整改任务数
     */
    private Integer resolvedRectificationTasks;

    /**
     * 待整改任务数（含待整改、待复核）
     */
    private Integer pendingRectificationTasks;

    /**
     * 逾期未整改任务数
     */
    private Integer overdueRectificationTasks;

    /**
     * 平均整改时长（天）
     * 计算方式：所有已销项任务的(销项时间 - 创建时间)的平均值
     */
    private Double averageRectificationDays;

    // ==================== 参与度指标 ====================

    /**
     * 参与户数（有积分记录的用户数）
     */
    private Integer participantCount;

    /**
     * 总户数（全村用户数）
     */
    private Integer totalHouseholds;

    /**
     * 参与率（百分比）
     * 计算方式：参与户数 / 总户数 * 100
     */
    private Double participationRate;

    /**
     * 参与率变化（与上月相比的百分点变化）
     * 正数表示提升，负数表示下降
     */
    private Double participationRateChange;

    /**
     * 新增参与户数（本月新参与的用户数）
     */
    private Integer newParticipants;

    /**
     * 活跃用户数（本月有积分变动的用户数）
     */
    private Integer activeUsers;

    // ==================== 红黑榜指标 ====================

    /**
     * 红榜增长率（百分比）
     * 计算方式：（本月红榜户数 - 上月红榜户数）/ 上月红榜户数 * 100
     */
    private Double redListGrowthRate;

    /**
     * 黑榜减少率（百分比）
     * 计算方式：（上月黑榜户数 - 本月黑榜户数）/ 上月黑榜户数 * 100
     */
    private Double blackListReductionRate;

    /**
     * 本月红榜户数
     */
    private Integer currentRedCount;

    /**
     * 上月红榜户数
     */
    private Integer previousRedCount;

    /**
     * 本月黑榜户数
     */
    private Integer currentBlackCount;

    /**
     * 上月黑榜户数
     */
    private Integer previousBlackCount;

    /**
     * 当前月份红榜户ID列表
     */
    private List<Long> currentRedList;

    /**
     * 当前月份黑榜户ID列表
     */
    private List<Long> currentBlackList;

    /**
     * 上个月份红榜户ID列表
     */
    private List<Long> previousRedList;

    /**
     * 上个月份黑榜户ID列表
     */
    private List<Long> previousBlackList;

    // ==================== 综合效果指标 ====================

    /**
     * 治理效果综合评分（0-100）
     * 由多种指标加权计算得出
     */
    private Integer overallScore;

    /**
     * 治理效果等级：优秀/良好/一般/待提升
     */
    private String grade;

    /**
     * 一句话总结（如："家禽散养问题显著改善"）
     */
    private String summary;

    // ==================== 便捷方法 ====================

    /**
     * 获取格式化整改完成率
     */
    public String getRectificationCompletionRateText() {
        if (rectificationCompletionRate == null) {
            return "0%";
        }
        return String.format("%.1f%%", rectificationCompletionRate);
    }

    /**
     * 获取格式化参与率
     */
    public String getParticipationRateText() {
        if (participationRate == null) {
            return "0%";
        }
        return String.format("%.1f%%", participationRate);
    }

    /**
     * 获取参与率变化文本
     */
    public String getParticipationRateChangeText() {
        if (participationRateChange == null) {
            return "持平";
        }
        if (participationRateChange > 0) {
            return "↑ +" + String.format("%.1f", participationRateChange) + "%";
        } else if (participationRateChange < 0) {
            return "↓ " + String.format("%.1f", participationRateChange) + "%";
        }
        return "持平";
    }

    /**
     * 获取红榜增长率文本
     */
    public String getRedListGrowthRateText() {
        if (redListGrowthRate == null) {
            return "持平";
        }
        if (redListGrowthRate > 0) {
            return "↑ +" + String.format("%.1f", redListGrowthRate) + "%";
        } else if (redListGrowthRate < 0) {
            return "↓ " + String.format("%.1f", redListGrowthRate) + "%";
        }
        return "持平";
    }

    /**
     * 获取黑榜减少率文本
     */
    public String getBlackListReductionRateText() {
        if (blackListReductionRate == null) {
            return "无变化";
        }
        if (blackListReductionRate > 0) {
            return "↓ " + String.format("%.1f", blackListReductionRate) + "%";
        } else if (blackListReductionRate < 0) {
            return "↑ " + String.format("%.1f", Math.abs(blackListReductionRate)) + "%";
        }
        return "无变化";
    }

    /**
     * 获取平均整改时长文本
     */
    public String getAverageRectificationDaysText() {
        if (averageRectificationDays == null) {
            return "暂无数据";
        }
        if (averageRectificationDays < 1) {
            return "1天内";
        }
        return String.format("%.1f天", averageRectificationDays);
    }

    /**
     * 判断参与率是否提升
     */
    public boolean isParticipationRateImproving() {
        return participationRateChange != null && participationRateChange > 0;
    }

    /**
     * 判断红榜是否增长
     */
    public boolean isRedListGrowing() {
        return redListGrowthRate != null && redListGrowthRate > 0;
    }

    /**
     * 判断黑榜是否减少
     */
    public boolean isBlackListReducing() {
        return blackListReductionRate != null && blackListReductionRate > 0;
    }

    /**
     * 获取治理等级对应的颜色
     */
    public String getGradeColor() {
        if (grade == null) {
            return "#999999";
        }
        switch (grade) {
            case "优秀":
                return "#52c41a";
            case "良好":
                return "#1890ff";
            case "一般":
                return "#faad14";
            case "待提升":
                return "#ff4d4f";
            default:
                return "#999999";
        }
    }

    /**
     * 获取治理等级对应的图标
     */
    public String getGradeIcon() {
        if (grade == null) {
            return "❓";
        }
        switch (grade) {
            case "优秀":
                return "🌟";
            case "良好":
                return "👍";
            case "一般":
                return "📊";
            case "待提升":
                return "📈";
            default:
                return "❓";
        }
    }

    /**
     * 获取红榜变化趋势描述
     */
    public String getRedTrend() {
        if (currentRedCount == null || previousRedCount == null) {
            return "";
        }
        int change = currentRedCount - previousRedCount;
        if (change > 0) {
            return "红榜+" + change + "户";
        } else if (change < 0) {
            return "红榜" + change + "户";
        } else {
            return "红榜持平";
        }
    }

    /**
     * 获取黑榜变化趋势描述
     */
    public String getBlackTrend() {
        if (currentBlackCount == null || previousBlackCount == null) {
            return "";
        }
        int change = currentBlackCount - previousBlackCount;
        if (change > 0) {
            return "黑榜+" + change + "户";
        } else if (change < 0) {
            return "黑榜" + change + "户";
        } else {
            return "黑榜持平";
        }
    }
}