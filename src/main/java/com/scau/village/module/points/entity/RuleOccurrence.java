package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则族发生记录实体，对应 rule_occurrence 表。
 * 用于记录家禽散养等阶梯规则的历史发生次数。
 */
@Data
@TableName("rule_occurrence")
public class RuleOccurrence {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户字段需显式映射，避免租户拦截器查询时字段缺失。 */
    @TableField("tenant_id")
    private Integer tenantId;

    private Integer userId;

    private String familyCode;

    private Integer occurrenceNo;

    private LocalDateTime eventTime;

    private String sourceApplyId;

    private String ruleVersion;

    private String idempotencyKey;

    private Integer cancelled;
}
