package com.scau.village.module.points.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 重要贡献认定提交请求参数。 */
@Data
public class ImportantContributionSubmitDto {

    /** 关联的积分规则 ID，仅支持重要贡献规则。 */
    @NotNull(message = "规则ID不能为空")
    private Integer ruleId;

    /** 贡献事迹描述。 */
    @NotBlank(message = "贡献描述不能为空")
    private String contributionDesc;

    /** 佐证照片 URL，多个地址用逗号分隔。 */
    private String evidencePhotos;

    /** 外部来源或业务单据引用。 */
    private String sourceRef;
}
