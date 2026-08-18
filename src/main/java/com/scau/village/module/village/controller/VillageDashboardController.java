package com.scau.village.module.village.controller;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.module.village.dto.StatsDto;
import com.scau.village.module.village.service.VillageStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class VillageDashboardController {

    private final VillageStatsService statsService;

    @GetMapping("/stats")
    public Result<StatsDto> getStats() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getTenantId() == null) {
            return Result.error(401, "请先登录");
        }
        String role = ctx.getRole();
        if (!"VILLAGE_ADMIN".equals(role) && !"TOWN_ADMIN".equals(role)) {
            return Result.error(403, "权限不足");
        }
        StatsDto stats = statsService.getStats(ctx.getTenantId());
        return Result.success(stats);
    }
}