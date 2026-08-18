package com.scau.village.module.warning.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.tenant.entity.Tenant;
import com.scau.village.module.tenant.mapper.TenantMapper;
import com.scau.village.module.warning.service.WarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarningTask {

    private final TenantMapper tenantMapper;
    private final PointsApplyMapper pointsApplyMapper;
    private final WarningService warningService;

    @Value("${warning.no-apply-days:14}")
    private int noApplyDays;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkNoApplyWarning() {
        List<Tenant> tenants = tenantMapper.selectList(null);
        LocalDateTime deadline = LocalDateTime.now().minusDays(noApplyDays);
        for (Tenant tenant : tenants) {
            Long count = pointsApplyMapper.selectCount(new LambdaQueryWrapper<PointsApply>()
                    .eq(PointsApply::getTenantId, tenant.getId())
                    .ge(PointsApply::getCreateTime, deadline));
            if (count == 0) {
                warningService.generateWarning(tenant.getId(), "NO_APPLY",
                        String.format("村庄[%s]已连续%d天无积分申报", tenant.getTenantName(), noApplyDays));
                log.info("生成预警: 村庄{}无申报", tenant.getTenantName());
            }
        }
    }
}