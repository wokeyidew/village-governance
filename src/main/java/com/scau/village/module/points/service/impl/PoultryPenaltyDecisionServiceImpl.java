package com.scau.village.module.points.service.impl;

import com.scau.village.module.points.service.PoultryPenaltyDecisionService;
import com.scau.village.module.points.service.RuleOccurrenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 家禽散养阶梯扣分决策服务实现。 */
@Service
@RequiredArgsConstructor
public class PoultryPenaltyDecisionServiceImpl implements PoultryPenaltyDecisionService {

    private static final String POULTRY_FAMILY = "poultry_free_range";

    private final RuleOccurrenceService ruleOccurrenceService;

    @Override
    public int resolveStep(int userId) {
        int nextOccurrenceNo = ruleOccurrenceService.nextOccurrenceNo(userId, POULTRY_FAMILY);
        if (nextOccurrenceNo <= 1) {
            return 1;
        }
        if (nextOccurrenceNo == 2) {
            return 2;
        }
        return 3;
    }

    @Override
    public int resolveRuleId(int userId) {
        int step = resolveStep(userId);
        if (step == 1) {
            return 43;
        }
        if (step == 2) {
            return 44;
        }
        return 45;
    }
}
