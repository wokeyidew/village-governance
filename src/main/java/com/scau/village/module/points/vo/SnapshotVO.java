package com.scau.village.module.points.vo;

import com.scau.village.module.points.entity.PublishSnapshot;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 红黑榜快照展示视图对象
 * 用于前端展示红黑榜榜单数据
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 字段类型从 Long 改为 String，与实体类 PublishSnapshot 保持一致
 * - 包括：batchId, publishBy
 * - redList 和 blackList 从 List<Long> 改为 List<String>
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class SnapshotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联批次ID（雪花ID字符串）
     */
    private String batchId;

    /**
     * 月份标识，格式：yyyy-MM
     */
    private String month;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布人ID（雪花ID字符串）
     */
    private String publishBy;

    /**
     * 发布人姓名
     */
    private String publishByName;

    /**
     * 总参与户数
     */
    private Integer totalItems;

    /**
     * 红榜户数
     */
    private Integer redCount;

    /**
     * 黑榜户数
     */
    private Integer blackCount;

    /**
     * 榜单条目列表（包含所有户）
     * 复用 PublishSnapshot.SnapshotItem 类型
     */
    private List<PublishSnapshot.SnapshotItem> items;

    /**
     * 红榜户ID列表（雪花ID字符串列表）
     */
    private List<String> redList;

    /**
     * 黑榜户ID列表（雪花ID字符串列表）
     */
    private List<String> blackList;

    // ==================== 手动 getter/setter ====================

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public String getPublishBy() {
        return publishBy;
    }

    public void setPublishBy(String publishBy) {
        this.publishBy = publishBy;
    }

    public String getPublishByName() {
        return publishByName;
    }

    public void setPublishByName(String publishByName) {
        this.publishByName = publishByName;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getRedCount() {
        return redCount;
    }

    public void setRedCount(Integer redCount) {
        this.redCount = redCount;
    }

    public Integer getBlackCount() {
        return blackCount;
    }

    public void setBlackCount(Integer blackCount) {
        this.blackCount = blackCount;
    }

    public List<PublishSnapshot.SnapshotItem> getItems() {
        return items;
    }

    public void setItems(List<PublishSnapshot.SnapshotItem> items) {
        this.items = items;
    }

    public List<String> getRedList() {
        return redList;
    }

    public void setRedList(List<String> redList) {
        this.redList = redList;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }
}