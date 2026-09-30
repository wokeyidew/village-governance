package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则子项定义实体，对应 rule_component 表。
 * 用于描述门前三包的卫生、绿化和秩序等可独立计分的组成项。
 */
@Data
@TableName("rule_component")
public class RuleComponent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户字段需显式映射，避免租户拦截器查询时字段缺失。 */
    @TableField("tenant_id")
    private Integer tenantId;

    private Integer ruleId;

    private String ruleVersion;

    private String componentCode;

    private String componentName;

    private Integer points;

    private Integer required;
}
