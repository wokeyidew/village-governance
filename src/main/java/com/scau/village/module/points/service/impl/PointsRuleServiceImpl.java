package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.PointsRuleService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@Service
public class PointsRuleServiceImpl extends ServiceImpl<PointsRuleMapper, PointsRule> implements PointsRuleService {

    @Override
    @Cacheable(value = "pointsRules", key = "#tenantId")
    public List<PointsRule> listByTenantId(Integer tenantId) {
        return lambdaQuery().eq(PointsRule::getTenantId, tenantId)
                .eq(PointsRule::getStatus, 1)
                .orderByAsc(PointsRule::getSortOrder)
                .list();
    }
}