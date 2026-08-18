package com.scau.village.module.moment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发布动态请求DTO
 *
 * @author system
 * @since 2026-07-17
 */
@Data
public class MomentDto {

    /**
     * 动态内容（必填）
     */
    @NotBlank(message = "动态内容不能为空")
    private String content;

    /**
     * 图片URL列表（可选），多个用逗号分隔
     */
    private String images;
}