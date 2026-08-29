package com.scau.village.module.rectification.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 管理员复核整改请求DTO
 * 用于管理员对村民已提交的整改任务进行复核
 *
 * 修复说明（2026-08-30）：
 * - taskId 保持 String 类型，前端传递雪花ID字符串，防止JS精度丢失
 * - reviewResult 增加 @Pattern 校验，限定只能为 passed 或 rejected
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class ReviewRectificationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 整改任务ID（必填）
     * 前端传递雪花ID字符串，防止JS精度丢失
     * 对应 rectification_task.id（VARCHAR(64)）
     */
    @NotBlank(message = "整改任务ID不能为空")
    private String taskId;

    /**
     * 复核结果（必填）
     * passed-通过，rejected-不通过
     */
    @NotBlank(message = "复核结果不能为空")
    @Pattern(regexp = "^(passed|rejected)$", message = "复核结果只能为 passed 或 rejected")
    private String reviewResult;

    /**
     * 复核备注（可选）
     * 管理员可填写复核意见或说明
     */
    private String reviewRemark;

}