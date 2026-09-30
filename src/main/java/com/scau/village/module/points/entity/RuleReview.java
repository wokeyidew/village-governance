package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则审核记录实体，对应 rule_review 表。
 * 用于追踪 double 审核流程的各个阶段。
 */
@Data
@TableName("rule_review")
public class RuleReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyId;

    private Integer stageNo;

    private Integer reviewerId;

    private String decision;

    private String remark;

    private LocalDateTime reviewTime;

    /** 租户字段需显式映射，避免租户拦截器查询时字段缺失。 */
    @TableField("tenant_id")
    private Integer tenantId;
}
