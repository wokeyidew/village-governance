package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则观察事件实体类。
 * 对应 rule_observation_event 表，用于记录混装和分类等可追踪事实。
 */
@Data
@TableName("rule_observation_event")
public class RuleObservationEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Integer tenantId;

    private Integer userId;

    private Integer ruleId;

    private String ruleVersion;

    private String eventCode;

    private LocalDateTime eventTime;

    private String sourceApplyId;

    private String evidenceId;

    private Integer createdBy;

    private String remark;
}
