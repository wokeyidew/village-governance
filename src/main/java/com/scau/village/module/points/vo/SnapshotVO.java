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
 * @author system
 * @since 2026-08-19
 */
@Data
public class SnapshotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联批次ID
     */
    private Long batchId;

    /**
     * 月份标识，格式：yyyy-MM
     */
    private String month;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布人ID
     */
    private Long publishBy;

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
     * 红榜户ID列表
     */
    private List<Long> redList;

    /**
     * 黑榜户ID列表
     */
    private List<Long> blackList;

    // ==================== 手动 getter/setter（确保 Lombok 未生效时编译通过） ====================

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
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

    public Long getPublishBy() {
        return publishBy;
    }

    public void setPublishBy(Long publishBy) {
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

    public List<Long> getRedList() {
        return redList;
    }

    public void setRedList(List<Long> redList) {
        this.redList = redList;
    }

    public List<Long> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<Long> blackList) {
        this.blackList = blackList;
    }
}