package com.scau.village.module.dashboard.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 治理驾驶舱 - 问题类型分布视图对象
 * 用于展示最常见扣分类型Top5（饼图数据）
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class ProblemTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则名称（扣分规则名称）
     * 如：庭院地面有明显垃圾杂物
     */
    private String ruleName;

    /**
     * 规则所属分类
     * 如：庭院环境、垃圾分类与处理、家禽管理
     */
    private String category;

    /**
     * 该类型被扣分的次数（总次数）
     */
    private Integer count;

    /**
     * 该类型扣分占总扣分的比例（百分比）
     * 计算方式：该类型扣分次数 / 总扣分次数 * 100
     */
    private Double percentage;

    /**
     * 该类型涉及的用户数（被扣分的户数）
     */
    private Integer userCount;

    /**
     * 该类型总扣分值（所有该类型扣分的绝对值之和）
     */
    private Integer totalPoints;

    /**
     * 平均每次扣分值
     * 计算方式：总扣分值 / 扣分次数
     */
    private Double averagePoints;

    /**
     * 排名（按扣分次数降序）
     * 1 表示扣分最多的类型
     */
    private Integer rank;

    /**
     * 前端展示颜色（由前端或后端预设）
     * 可预设一组颜色，按排名分配
     */
    private String color;

    // ==================== 便捷方法 ====================

    /**
     * 获取格式化百分比
     * 如 "23.5%"
     */
    public String getPercentageText() {
        if (percentage == null) {
            return "0%";
        }
        return String.format("%.1f%%", percentage);
    }

    /**
     * 获取该类型的风险等级
     * 根据排名：Top1为"高危"，Top2-3为"中危"，Top4-5为"低危"
     */
    public String getRiskLevel() {
        if (rank == null) {
            return "未知";
        }
        switch (rank) {
            case 1:
                return "高危";
            case 2:
            case 3:
                return "中危";
            case 4:
            case 5:
                return "低危";
            default:
                return "正常";
        }
    }

    /**
     * 获取平均分的格式化文本
     */
    public String getAveragePointsText() {
        if (averagePoints == null) {
            return "0.0";
        }
        return String.format("%.1f", averagePoints);
    }
}