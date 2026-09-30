package com.scau.village.module.points.service;

import com.scau.village.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** 持续时间约束服务，供规则 #37 的 48 小时门槛使用。 */
@Service
public class DurationConstraintService {

    /** 计算两个观察时间之间的完整小时数。 */
    public long calculateHours(LocalDateTime firstObservedAt, LocalDateTime observedAt) {
        if (firstObservedAt == null || observedAt == null
                || observedAt.isBefore(firstObservedAt)) {
            throw new BusinessException("垃圾堆积观察时间无效");
        }
        return ChronoUnit.HOURS.between(firstObservedAt, observedAt);
    }

    /** 要求持续时间严格超过指定小时数。 */
    public void assertExceeded(LocalDateTime firstObservedAt, LocalDateTime observedAt,
                               long thresholdHours) {
        if (calculateHours(firstObservedAt, observedAt) <= thresholdHours) {
            throw new BusinessException("垃圾堆积未超过48小时");
        }
    }
}
