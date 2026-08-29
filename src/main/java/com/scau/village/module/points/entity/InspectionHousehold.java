package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 检查户汇总实体类
 * 对应表名：inspection_household
 * 用于记录每户每次检查的总得分，便于按户维度导出
 *
 * 修复说明（2026-08-30）：
 * - id、batchId、inspectorId 改为 String 类型（雪花ID），避免前端精度丢失
 * - userId 和 tenantId 保持 Long（自增ID或租户ID，不是雪花ID）
 *
 * @author system
 * @since 2026-07-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("inspection_household")
public class InspectionHousehold implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花ID字符串）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 关联的检查批次ID（inspection_batch.id，雪花ID字符串）
     */
    private String batchId;

    /**
     * 户主用户ID（关联user表，自增ID）
     */
    private Long userId;

    /**
     * 本次检查总得分（可为负数，若扣分项多）
     */
    private Integer totalScore;

    /**
     * 详细得分JSON（可选），存储格式如：
     * [{"ruleId":1,"ruleName":"庭院整洁","score":5},{"ruleId":2,"ruleName":"垃圾分类","score":-2}]
     * 便于前端展示明细，也可不存，通过关联查询实时计算
     */
    private String detailJson;

    /**
     * 检查员ID（管理员ID，雪花ID字符串）
     */
    private String inspectorId;

    /**
     * 检查备注
     */
    private String remark;

    /**
     * 租户ID
     */
    private Long tenantId;

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
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;

    // ==================== 手动 getter/setter（确保 Lombok 未生效时编译通过） ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }

    public String getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(String inspectorId) {
        this.inspectorId = inspectorId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}