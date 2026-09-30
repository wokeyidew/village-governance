package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.module.points.entity.RuleComponent;
import com.scau.village.module.points.entity.RuleComponentResult;
import com.scau.village.module.points.mapper.RuleComponentMapper;
import com.scau.village.module.points.mapper.RuleComponentResultMapper;
import com.scau.village.module.points.service.RuleComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 规则子项定义与结果服务实现。 */
@Service
@RequiredArgsConstructor
public class RuleComponentServiceImpl extends ServiceImpl<RuleComponentMapper, RuleComponent>
        implements RuleComponentService {

    private final RuleComponentResultMapper ruleComponentResultMapper;

    @Override
    public List<RuleComponent> listByRule(int ruleId) {
        LambdaQueryWrapper<RuleComponent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleComponent::getTenantId, UserContext.getCurrentTenantId())
                .eq(RuleComponent::getRuleId, ruleId)
                .orderByAsc(RuleComponent::getId);
        return list(wrapper);
    }

    @Override
    public int calculateScore(int ruleId, Map<String, Boolean> componentResults) {
        int score = 0;
        for (RuleComponent component : listByRule(ruleId)) {
            if (componentResults != null
                    && Boolean.TRUE.equals(componentResults.get(component.getComponentCode()))) {
                score += component.getPoints() == null ? 0 : component.getPoints();
            }
        }
        return score;
    }

    @Override
    @Transactional
    public List<RuleComponentResult> saveResults(String applyId, int ruleId,
                                                  Map<String, Boolean> componentResults) {
        List<RuleComponentResult> results = new ArrayList<>();
        for (RuleComponent component : listByRule(ruleId)) {
            RuleComponentResult result = new RuleComponentResult();
            result.setApplyId(applyId);
            result.setComponentId(component.getId());
            result.setResult(componentResults != null
                    && Boolean.TRUE.equals(componentResults.get(component.getComponentCode()))
                    ? "pass" : "fail");
            ruleComponentResultMapper.insert(result);
            results.add(result);
        }
        return results;
    }
}
