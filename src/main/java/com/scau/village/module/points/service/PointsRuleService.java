package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.PointsRule;
import java.util.List;

public interface PointsRuleService extends IService<PointsRule> {
    List<PointsRule> listByTenantId(Integer tenantId);
}