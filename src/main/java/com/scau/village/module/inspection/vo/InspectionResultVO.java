package com.scau.village.module.inspection.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 村民端查看评比结果VO
 * 用于村民查看自己在该批次中的得分明细
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class InspectionResultVO {

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 批次名称
     */
    private String batchName;

    /**
     * 检查日期
     */
    private LocalDate inspectionDate;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    /**
     * 是否已发布
     */
    private Boolean isPublished;

    /**
     * 当前用户ID
     */
    private Integer userId;

    /**
     * 当前用户姓名
     */
    private String userName;

    /**
     * 当前用户总得分
     */
    private Integer totalScore;

    /**
     * 加分合计
     */
    private Integer totalBonus;

    /**
     * 扣分合计
     */
    private Integer totalPenalty;

    /**
     * 得分明细列表（每条规则一条记录）
     */
    private List<ScoreDetail> details;

    /**
     * 得分明细内部类
     */
    @Data
    public static class ScoreDetail {

        /**
         * 规则ID
         */
        private Integer ruleId;

        /**
         * 规则名称
         */
        private String ruleName;

        /**
         * 规则类别（如：庭院环境、垃圾分类等）
         */
        private String category;

        /**
         * 得分（正数为加分，负数为扣分）
         */
        private Integer score;

        /**
         * 规则说明/描述
         */
        private String description;

        /**
         * 是否加分项（true-加分，false-扣分）
         */
        private Boolean isBonus;
    }
}