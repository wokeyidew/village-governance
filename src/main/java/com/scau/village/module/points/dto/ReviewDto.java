package com.scau.village.module.points.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** double 审核请求参数。 */
@Data
public class ReviewDto {

    /** 审核决定：approved 或 rejected。 */
    @NotBlank(message = "审核决定不能为空")
    private String decision;

    /** 审核备注。 */
    private String remark;
}
