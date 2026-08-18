package com.scau.village.module.feedback.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 提交反馈请求DTO
 * 用于村民提交意见反馈时的请求参数
 *
 * @author system
 * @since 2026-07-23
 */
@Data
public class FeedbackSubmitDto {

    /**
     * 反馈类型
     * complaint-投诉，suggestion-建议，question-咨询，other-其他
     */
    @NotBlank(message = "反馈类型不能为空")
    private String category;

    /**
     * 反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String content;

    /**
     * 图片URL列表（逗号分隔）
     * 可选字段，用于上传相关图片证据
     */
    private String images;

    /**
     * 联系方式（可选）
     * 便于管理员联系反馈人
     */
    @Size(max = 64, message = "联系方式不能超过64个字符")
    private String contact;
}