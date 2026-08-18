package com.scau.village.module.points.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 月度红黑榜对比视图对象
 * 用于展示两个月份之间的红黑榜变化情况
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class CompareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前月份，格式：yyyy-MM
     */
    private String currentMonth;

    /**
     * 对比月份，格式：yyyy-MM
     */
    private String previousMonth;

    /**
     * 当前月份红榜户数
     */
    private Integer currentRedCount;

    /**
     * 对比月份红榜户数
     */
    private Integer previousRedCount;

    /**
     * 当前月份黑榜户数
     */
    private Integer currentBlackCount;

    /**
     * 对比月份黑榜户数
     */
    private Integer previousBlackCount;

    /**
     * 红榜新增户数（当前月红榜 - 对比月红榜）
     */
    private Integer redAdded;

    /**
     * 红榜退出户数（对比月红榜 - 当前月红榜）
     */
    private Integer redExited;

    /**
     * 黑榜新增户数（当前月黑榜 - 对比月黑榜）
     */
    private Integer blackAdded;

    /**
     * 黑榜退出户数（对比月黑榜 - 当前月黑榜）
     */
    private Integer blackExited;

    /**
     * 红榜新增用户ID列表
     */
    private List<Long> redAddedList;

    /**
     * 红榜退出用户ID列表
     */
    private List<Long> redExitedList;

    /**
     * 黑榜新增用户ID列表
     */
    private List<Long> blackAddedList;

    /**
     * 黑榜退出用户ID列表
     */
    private List<Long> blackExitedList;

    /**
     * 用户排名变化列表（包含所有在两个月份都有记录的用户）
     */
    private List<RankChange> rankChanges;

    // ==================== 内部类：排名变化 ====================

    @Data
    public static class RankChange implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户姓名
         */
        private String userName;

        /**
         * 对比月份排名
         */
        private Integer previousRank;

        /**
         * 当前月份排名
         */
        private Integer currentRank;

        /**
         * 排名变化值（当前排名 - 对比排名）
         * 负数表示名次上升（如 -2 表示上升了2名）
         * 正数表示名次下降
         * null 表示数据缺失
         */
        private Integer change;

        // ==================== 便捷方法 ====================

        /**
         * 获取排名变化的文本描述
         */
        public String getChangeText() {
            if (change == null) {
            return "数据缺失";
            }
            if (change == 0) {
                return "持平";
            }
            return change < 0 ? "上升" + Math.abs(change) + "名" : "下降" + change + "名";
        }

        /**
         * 获取排名变化的符号（用于UI显示）
         * 上升显示绿色箭头，下降显示红色箭头
         */
        public String getChangeSymbol() {
            if (change == null) {
                return "-";
            }
            if (change == 0) {
                return "→";
            }
            return change < 0 ? "↑" : "↓";
        }

        /**
         * 判断排名是否上升
         */
        public boolean isImproving() {
            return change != null && change < 0;
        }

        /**
         * 判断排名是否下降
         */
        public boolean isDeclining() {
            return change != null && change > 0;
        }

        /**
         * 判断排名是否持平
         */
        public boolean isStable() {
            return change != null && change == 0;
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取红榜变化趋势描述
     */
    public String getRedTrend() {
        if (redAdded == null || redExited == null) {
            return "";
        }
        if (redAdded == 0 && redExited == 0) {
            return "红榜户数无变化";
        }
        int netChange = redAdded - redExited;
        if (netChange > 0) {
            return "红榜户数增加" + netChange + "户";
        } else if (netChange < 0) {
            return "红榜户数减少" + Math.abs(netChange) + "户";
        } else {
            return "红榜户数持平（新增" + redAdded + "户，退出" + redExited + "户）";
        }
    }

    /**
     * 获取黑榜变化趋势描述
     */
    public String getBlackTrend() {
        if (blackAdded == null || blackExited == null) {
            return "";
        }
        if (blackAdded == 0 && blackExited == 0) {
            return "黑榜户数无变化";
        }
        int netChange = blackAdded - blackExited;
        if (netChange > 0) {
            return "黑榜户数增加" + netChange + "户";
        } else if (netChange < 0) {
            return "黑榜户数减少" + Math.abs(netChange) + "户";
        } else {
            return "黑榜户数持平（新增" + blackAdded + "户，退出" + blackExited + "户）";
        }
    }
}