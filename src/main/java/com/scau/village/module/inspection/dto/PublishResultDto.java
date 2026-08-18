package com.scau.village.module.inspection.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 评比结果发布请求DTO
 * 用于管理员发布检查批次的评比结果
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class PublishResultDto {

    /**
     * 检查批次ID（必填）
     */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    /**
     * 发布备注（可选）
     */
    private String remark;
}