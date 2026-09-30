package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.module.points.entity.RuleObservationEvent;
import com.scau.village.module.points.mapper.RuleObservationEventMapper;
import com.scau.village.module.points.service.RuleObservationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 规则观察事件服务实现。
 */
@Service
@RequiredArgsConstructor
public class RuleObservationEventServiceImpl
        extends ServiceImpl<RuleObservationEventMapper, RuleObservationEvent>
        implements RuleObservationEventService {

    @Override
    public boolean exists(int userId, int ruleId, String eventCode,
                          LocalDateTime start, LocalDateTime end) {
        if (eventCode == null || start == null || end == null || start.isAfter(end)) {
            return false;
        }
        LambdaQueryWrapper<RuleObservationEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleObservationEvent::getTenantId, UserContext.getCurrentTenantId())
                .eq(RuleObservationEvent::getUserId, userId)
                .eq(RuleObservationEvent::getRuleId, ruleId)
                .eq(RuleObservationEvent::getEventCode, eventCode)
                .ge(RuleObservationEvent::getEventTime, start)
                .le(RuleObservationEvent::getEventTime, end)
                .last("LIMIT 1");
        return count(wrapper) > 0;
    }

    @Override
    @Transactional
    public RuleObservationEvent record(int userId, int ruleId, String eventCode,
                                       LocalDateTime eventTime, String applyId) {
        if (eventCode == null || eventTime == null) {
            throw new IllegalArgumentException("观察事件编码和时间不能为空");
        }
        RuleObservationEvent event = new RuleObservationEvent();
        event.setTenantId(UserContext.getCurrentTenantId());
        event.setUserId(userId);
        event.setRuleId(ruleId);
        // 当前调用方只记录初始规则版本；规则版本切换时需由调用方传入版本。
        // TODO: 需人工确认观察事件版本是否需要从 rule_constraint 自动回溯。
        event.setRuleVersion("1.0");
        event.setEventCode(eventCode);
        event.setEventTime(eventTime);
        event.setSourceApplyId(applyId);
        event.setCreatedBy(UserContext.getCurrentUserId() == null
                ? null : UserContext.getCurrentUserId().intValue());
        save(event);
        return event;
    }
}
