package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@TableName("points_rule")
public class PointsRule {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer tenantId;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则类别不能为空")
    private String category;

    @NotNull(message = "积分值不能为空")
    private Integer points;

    @NotBlank(message = "审核流程不能为空")
    private String auditFlow;

    private Integer needPhoto;

    private Integer maxTimesPerDay;

    /** 规则族编码，用于识别需要统一决策的阶梯规则。 */
    @TableField("rule_family_code")
    private String ruleFamilyCode;

    /** 规则在规则族中的阶梯序号。 */
    @TableField("step_no")
    private Integer stepNo;

    /** 规则版本号，用于追踪规则变更。 */
    @TableField("rule_version")
    private String ruleVersion;

    private Integer status;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
