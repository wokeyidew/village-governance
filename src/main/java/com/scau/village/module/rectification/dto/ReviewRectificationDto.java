package com.scau.village.module.rectification.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 管理员复核整改请求DTO
 * 用于管理员对村民已提交的整改任务进行复核
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class ReviewRectificationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 整改任务ID（必填）
     */
    @NotNull(message = "整改任务ID不能为空")
    private Long taskId;

    /**
     * 复核结果（必填）
     * passed-通过，rejected-不通过
     */
    @NotBlank(message = "复核结果不能为空")
    private String reviewResult;

    /**
     * 复核备注（可选）
     * 管理员可填写复核意见或说明
     */
    private String reviewRemark;
}