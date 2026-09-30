package com.scau.village.module.points.service;

import com.scau.village.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 规则 #37 垃圾堆积持续时间约束单元测试。 */
class DurationConstraintServiceTest {

    private DurationConstraintService service;
    private LocalDateTime firstObservedAt;

    @BeforeEach
    void setUp() {
        service = new DurationConstraintService();
        firstObservedAt = LocalDateTime.of(2026, 10, 1, 8, 0);
    }

    @Test
    void fortySevenHoursDoesNotMeetDeductionThreshold() {
        LocalDateTime observedAt = firstObservedAt.plusHours(47);

        assertThatThrownBy(() -> service.assertExceeded(firstObservedAt, observedAt, 48))
                .isInstanceOf(BusinessException.class)
                .hasMessage("垃圾堆积未超过48小时");
    }

    @Test
    void fortyNineHoursMeetsDeductionThreshold() {
        LocalDateTime observedAt = firstObservedAt.plusHours(49);

        service.assertExceeded(firstObservedAt, observedAt, 48);

        assertThat(service.calculateHours(firstObservedAt, observedAt)).isEqualTo(49);
    }

    @Test
    void exactlyFortyEightHoursIsNotMoreThanThreshold() {
        LocalDateTime observedAt = firstObservedAt.plusHours(48);

        assertThatThrownBy(() -> service.assertExceeded(firstObservedAt, observedAt, 48))
                .isInstanceOf(BusinessException.class)
                .hasMessage("垃圾堆积未超过48小时");
    }
}
