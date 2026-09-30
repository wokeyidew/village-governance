package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则子项结果实体，对应 rule_component_result 表。
 * 每次评分申请为每个定义中的子项保存一条通过或不通过结果。
 */
@Data
@TableName("rule_component_result")
public class RuleComponentResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyId;

    private Long componentId;

    /** 结果值使用 pass/fail，与数据库 result 字段约定保持一致。 */
    private String result;

    private String evidenceId;

    private String remark;
}
