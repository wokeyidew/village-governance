package com.scau.village.module.village.service;

import com.scau.village.module.village.dto.StatsDto;

public interface VillageStatsService {
    StatsDto getStats(Integer tenantId);
}