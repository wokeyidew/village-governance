package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.RuleObservationEvent;

import java.time.LocalDateTime;

/**
 * 规则观察事件服务。
 */
public interface RuleObservationEventService extends IService<RuleObservationEvent> {

    /**
     * 查询时间窗口内是否存在指定观察事件。
     */
    boolean exists(int userId, int ruleId, String eventCode,
                   LocalDateTime start, LocalDateTime end);

    /**
     * 记录一条规则观察事件。
     */
    RuleObservationEvent record(int userId, int ruleId, String eventCode,
                                LocalDateTime eventTime, String applyId);
}
