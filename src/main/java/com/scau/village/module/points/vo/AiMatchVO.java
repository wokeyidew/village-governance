package com.scau.village.module.points.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * AI匹配结果视图对象
 * 用于前端展示AI推荐的积分规则
 * 
 * 当 AI 服务不可用时，后端会返回降级模式数据（isDemo = true），
 * 前端可根据该字段展示“AI演示”标识，避免用户误以为真实AI服务已生效
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class AiMatchVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 匹配到的规则ID（数据库中的规则ID）
     * 当 AI 服务返回规则索引时，前端可根据索引从本地规则列表中获取规则ID
     */
    private Long ruleId;

    /**
     * 匹配到的规则索引（当规则ID无法获取时使用，用于前端从规则列表中查找）
     * 注意：优先使用 ruleId，若 ruleId 为空则使用 ruleIndex
     */
    private Integer ruleIndex;

    /**
     * 匹配到的规则名称（CLIP模型返回的规则描述文本）
     */
    private String ruleName;

    /**
     * 匹配置信度（0-1之间的浮点数）
     */
    private Double confidence;

    /**
     * 匹配置信度百分比（转换为百分数，如 72.5）
     */
    private Double confidencePercent;

    /**
     * 建议操作：加分 / 扣分
     */
    private String suggestedAction;

    /**
     * 建议积分（正数表示加分，负数表示扣分）
     */
    private Integer suggestedPoints;

    /**
     * 置信度等级：高、中、低
     */
    private String confidenceLevel;

    /**
     * 是否为降级模式（AI 服务不可用时的预设结果）
     * true：AI 服务不可用，返回的是预设演示结果
     * false：AI 服务正常，返回的是真实匹配结果
     */
    private Boolean isDemo;

    // ==================== 便捷方法 ====================

    /**
     * 获取格式化置信度文本（如 "72.5%"）
     */
    public String getConfidenceText() {
        if (confidence == null) {
            return "0%";
        }
        return String.format("%.1f%%", confidence * 100);
    }

    /**
     * 获取置信度等级
     * 根据置信度阈值返回高/中/低
     */
    public String getConfidenceLevel() {
        if (confidence == null) {
            return "低";
        }
        if (confidence >= 0.7) {
            return "高";
        } else if (confidence >= 0.4) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 判断是否可信（置信度 >= 0.7 为可信）
     */
    public boolean isReliable() {
        return confidence != null && confidence >= 0.7;
    }
}