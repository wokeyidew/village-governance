package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则约束实体类。
 * 对应 rule_constraint 表，保存规则版本、窗口和审核约束。
 */
@Data
@TableName("rule_constraint")
public class RuleConstraint {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Integer tenantId;

    private Integer ruleId;

    private String ruleVersion;

    private String windowType;

    private Integer windowValue;

    private Integer maxTimes;

    private Integer requirePhoto;

    private String requireReviewFlow;

    private Integer minPeriodDays;

    private String minFrequency;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}
