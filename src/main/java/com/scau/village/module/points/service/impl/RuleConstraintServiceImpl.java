package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.points.entity.RuleConstraint;
import com.scau.village.module.points.mapper.RuleConstraintMapper;
import com.scau.village.module.points.service.RuleConstraintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 规则约束服务实现。
 */
@Service
@RequiredArgsConstructor
public class RuleConstraintServiceImpl extends ServiceImpl<RuleConstraintMapper, RuleConstraint>
        implements RuleConstraintService {

    @Override
    public RuleConstraint getEffective(int ruleId, LocalDateTime now) {
        LocalDateTime effectiveNow = now == null ? LocalDateTime.now() : now;
        LambdaQueryWrapper<RuleConstraint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleConstraint::getRuleId, ruleId)
                .le(RuleConstraint::getEffectiveFrom, effectiveNow)
                .and(query -> query.isNull(RuleConstraint::getEffectiveTo)
                        .or()
                        .gt(RuleConstraint::getEffectiveTo, effectiveNow))
                .orderByDesc(RuleConstraint::getEffectiveFrom)
                .last("LIMIT 1");
        return getOne(wrapper, false);
    }

    @Override
    public Window resolve(RuleConstraint constraint, LocalDateTime now) {
        if (constraint == null) {
            throw new IllegalArgumentException("规则约束不能为空");
        }
        if (now == null) {
            now = LocalDateTime.now();
        }
        String windowType = constraint.getWindowType();
        int windowValue = constraint.getWindowValue() == null ? 0 : constraint.getWindowValue();
        if ("rolling_days".equals(windowType)) {
            return new Window(now.minusDays(windowValue), now);
        }
        if ("natural_month".equals(windowType)) {
            return new Window(now.toLocalDate().withDayOfMonth(1).atStartOfDay(), now);
        }
        // TODO: 需人工确认未知窗口类型的制度解释。
        throw new IllegalArgumentException("不支持的规则窗口类型: " + windowType);
    }

    @Override
    public int countInWindow(int userId, int ruleId, Window window) {
        if (window == null || window.getStart() == null || window.getEnd() == null
                || window.getStart().isAfter(window.getEnd())) {
            return 0;
        }
        return baseMapper.countApprovedInWindow(userId, ruleId,
                window.getStart(), window.getEnd());
    }
}
