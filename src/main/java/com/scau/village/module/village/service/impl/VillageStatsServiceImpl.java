package com.scau.village.module.village.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import com.scau.village.module.village.dto.StatsDto;
import com.scau.village.module.village.service.VillageStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VillageStatsServiceImpl implements VillageStatsService {

    private final PointsApplyMapper pointsApplyMapper;
    private final UserMapper userMapper;

    @Override
    public StatsDto getStats(Integer tenantId) {
        // 如果 tenantId 为 null，返回空数据
        if (tenantId == null) {
            log.warn("tenantId 为空，返回默认统计数据");
            return new StatsDto();
        }
        try {
            StatsDto dto = new StatsDto();
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            
            dto.setTodayApplyCount(pointsApplyMapper.selectCount(
                new LambdaQueryWrapper<PointsApply>()
                    .eq(PointsApply::getTenantId, tenantId)
                    .ge(PointsApply::getCreateTime, todayStart)
            ));
            
            dto.setPendingAuditCount(pointsApplyMapper.selectCount(
                new LambdaQueryWrapper<PointsApply>()
                    .eq(PointsApply::getTenantId, tenantId)
                    .eq(PointsApply::getStatus, "pending")
            ));
            
            dto.setTotalPoints(userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .eq(User::getTenantId, tenantId)
            ).stream().mapToInt(User::getPoints).sum());
            
            dto.setParticipantCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .eq(User::getTenantId, tenantId)
            ));
            
            return dto;
        } catch (Exception e) {
            log.error("获取统计数据失败，tenantId: {}", tenantId, e);
            // 异常时返回空数据，避免前端报错
            return new StatsDto();
        }
    }
}