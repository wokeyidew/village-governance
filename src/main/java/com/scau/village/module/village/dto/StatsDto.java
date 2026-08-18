package com.scau.village.module.village.dto;

import lombok.Data;

@Data
public class StatsDto {
    private Long todayApplyCount;
    private Long pendingAuditCount;
    private Integer totalPoints;
    private Long participantCount;
    // 还可以加月度趋势等
}