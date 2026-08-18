package com.scau.village.module.feedback.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理员回复反馈请求DTO
 * 用于管理员对反馈进行回复并可选更新状态
 *
 * @author system
 * @since 2026-07-23
 */
@Data
public class FeedbackReplyDto {

    /**
     * 反馈ID（必填）
     */
    @NotNull(message = "反馈ID不能为空")
    private Integer id;

    /**
     * 回复内容（必填）
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500字")
    private String reply;

    /**
     * 更新后的状态（可选）
     * 如果不传，默认将状态改为 resolved（已处理）
     * 可选值：pending-待处理，processing-处理中，resolved-已处理，closed-已关闭
     */
    private String status;
}