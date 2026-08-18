package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查批次实体类
 * 对应表名：inspection_batch
 *
 * @author system
 * @since 2026-07-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("inspection_batch")
public class InspectionBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（使用雪花算法）
     * 序列化为字符串，避免前端 JavaScript 精度丢失（Long 超出 JS Number 安全范围）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 批次名称（如："2026年7月人居环境检查"）
     */
    private String batchName;

    /**
     * 检查日期
     */
    private LocalDate inspectionDate;

    /**
     * 检查范围描述（如：全村 / 第1-3组 / 龙胜村1组等）
     */
    private String scope;

    /**
     * 检查小组/负责人（可选）
     */
    private String inspectorGroup;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 租户ID（多租户隔离）
     */
    private Long tenantId;

    /**
     * 创建人ID（管理员）
     */
    private Long createBy;

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
}