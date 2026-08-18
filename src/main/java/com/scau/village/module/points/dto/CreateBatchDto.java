package com.scau.village.module.points.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 创建检查批次请求DTO
 *
 * @author system
 * @since 2026-07-16
 */
@Data
public class CreateBatchDto {

    /**
     * 批次名称（如："2026年7月人居环境检查"）
     */
    @NotBlank(message = "批次名称不能为空")
    private String batchName;

    /**
     * 检查日期
     */
    @NotNull(message = "检查日期不能为空")
    private LocalDate inspectionDate;

    /**
     * 检查范围描述（如：全村 / 第1-3组 / 龙胜村1组等）
     */
    private String scope;

    /**
     * 检查小组/负责人（可选）
     */
    private String inspectorGroup;

    /**
     * 备注信息
     */
    private String remark;
}