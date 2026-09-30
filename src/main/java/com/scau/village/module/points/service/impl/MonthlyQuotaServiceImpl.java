package com.scau.village.module.points.service.impl;

import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.RuleConstraint;
import com.scau.village.module.points.service.MonthlyQuotaService;
import com.scau.village.module.points.service.RuleConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 规则月度频次配额服务实现。 */
@Service
@RequiredArgsConstructor
public class MonthlyQuotaServiceImpl implements MonthlyQuotaService {

    private final RuleConstraintService ruleConstraintService;

    @Override
    public void assertAvailable(int userId, int ruleId) {
        RuleConstraint constraint = ruleConstraintService.getEffective(ruleId, LocalDateTime.now());
        if (constraint == null || constraint.getMaxTimes() == null
                || constraint.getMaxTimes() == 0) {
            return;
        }
        int remaining = getRemaining(userId, ruleId, constraint, LocalDateTime.now());
        if (remaining <= 0) {
            throw new BusinessException("该规则本月已达上限（" + constraint.getMaxTimes() + "次）");
        }
    }

    @Override
    public int getRemaining(int userId, int ruleId) {
        LocalDateTime now = LocalDateTime.now();
        RuleConstraint constraint = ruleConstraintService.getEffective(ruleId, now);
        if (constraint == null || constraint.getMaxTimes() == null
                || constraint.getMaxTimes() == 0) {
            return -1;
        }
        return getRemaining(userId, ruleId, constraint, now);
    }

    private int getRemaining(int userId, int ruleId, RuleConstraint constraint,
                             LocalDateTime now) {
        RuleConstraintService.Window window = ruleConstraintService.resolve(constraint, now);
        int used = ruleConstraintService.countInWindow(userId, ruleId, window);
        return Math.max(0, constraint.getMaxTimes() - used);
    }
}
