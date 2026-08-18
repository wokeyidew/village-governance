package com.scau.village.module.points.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 查询历史检查记录请求DTO
 * 支持按批次、按户、按日期范围筛选
 *
 * @author system
 * @since 2026-07-16
 */
@Data
public class InspectionQueryDto {

    /**
     * 批次ID（可选，按批次筛选）
     */
    private Long batchId;

    /**
     * 用户ID（可选，按户筛选）
     */
    private Integer userId;

    /**
     * 开始日期（可选，按日期范围筛选）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（可选，按日期范围筛选）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 当前页码（默认1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    /**
     * 每页大小（默认10）
     */
    @Min(value = 1, message = "每页大小最小为1")
    private Integer size = 10;

    /**
     * 来源类型（可选，默认admin，只查管理员评分记录）
     * 若需查村民申报可传user，为保持灵活性保留此字段
     */
    private String sourceType = "admin";
}