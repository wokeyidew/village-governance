package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 红黑榜公示快照实体类
 * 对应表名：publish_snapshot
 * 用于存储每月/每批次发布的红黑榜快照数据
 * 发布后数据冻结，不受后续评分修改影响
 *
 * 包含字段：id, batchId, month, snapshotData, redList, blackList,
 * publishTime, publishBy, publishByName, tenantId, createTime, updateTime, deleted
 * 以及内部类 SnapshotItem
 *
 * @author system
 * @since 2026-08-19
 */
@Data
@TableName("publish_snapshot")
public class PublishSnapshot {

    /**
     * 主键ID（雪花算法）
     * 序列化为字符串，避免前端 JavaScript 精度丢失（Long 超出 JS Number 安全范围）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 关联检查批次ID（inspection_batch.id）
     * 序列化为字符串，避免前端 JavaScript 精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long batchId;

    /**
     * 月份标识，格式：yyyy-MM
     * 例如：2026-08
     */
    private String month;

    /**
     * 全量快照数据（JSON格式）
     * 包含所有户的排名、分数、标签等信息
     * 格式示例：
     * [
     *   {"userId":1, "userName":"邓金超", "totalScore":95, "rank":1, "tag":"red"},
     *   {"userId":2, "userName":"李四", "totalScore":45, "rank":85, "tag":"black"}
     * ]
     */
    private String snapshotData;

    /**
     * 红榜户ID列表（JSON格式）
     * 示例：[1, 3, 5, 7]
     */
    private String redList;

    /**
     * 黑榜户ID列表（JSON格式）
     * 示例：[2, 4, 6, 8]
     */
    private String blackList;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布人ID（管理员）
     * 序列化为字符串，避免前端 JavaScript 精度丢失
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long publishBy;

    /**
     * 发布人姓名（冗余存储）
     */
    private String publishByName;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    // ==================== 内部类：榜单条目 ====================

    /**
     * 榜单条目（用于JSON序列化）
     */
    @Data
    public static class SnapshotItem {
        /**
         * 用户ID
         * 序列化为字符串，避免前端 JavaScript 精度丢失
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long userId;

        private String userName;
        private Integer totalScore;
        private Integer rank;
        private String tag; // "red" 或 "black" 或 "normal"

        // ==================== 便捷方法 ====================

        /**
         * 获取标签的中文描述
         */
        public String getTagText() {
            if (tag == null) return "";
            switch (tag) {
                case "red": return "红榜";
                case "black": return "黑榜";
                case "normal": return "普通";
                default: return tag;
            }
        }

        /**
         * 判断是否为红榜
         */
        public boolean isRed() {
            return "red".equals(tag);
        }

        /**
         * 判断是否为黑榜
         */
        public boolean isBlack() {
            return "black".equals(tag);
        }

        /**
         * 判断是否为普通户
         */
        public boolean isNormal() {
            return "normal".equals(tag);
        }

        /**
         * 获取总得分显示文本（带正负号）
         */
        public String getTotalScoreText() {
            if (totalScore == null) return "0";
            return totalScore >= 0 ? "+" + totalScore : String.valueOf(totalScore);
        }
    }
}