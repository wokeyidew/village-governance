package com.scau.village.module.points.service;

import com.scau.village.module.points.entity.RuleConstraint;
import com.scau.village.module.points.service.impl.RuleConstraintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 规则约束服务单元测试。
 */
class RuleConstraintServiceTest {

    private RuleConstraintServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RuleConstraintServiceImpl();
    }

    @Test
    void resolveRollingDaysWindow() {
        RuleConstraint constraint = new RuleConstraint();
        constraint.setWindowType("rolling_days");
        constraint.setWindowValue(30);
        LocalDateTime now = LocalDateTime.of(2026, 10, 1, 12, 30);

        RuleConstraintService.Window window = service.resolve(constraint, now);

        assertThat(window.getStart()).isEqualTo(LocalDateTime.of(2026, 9, 1, 12, 30));
        assertThat(window.getEnd()).isEqualTo(now);
    }

    @Test
    void resolveNaturalMonthWindow() {
        RuleConstraint constraint = new RuleConstraint();
        constraint.setWindowType("natural_month");
        constraint.setWindowValue(1);
        LocalDateTime now = LocalDateTime.of(2026, 10, 18, 9, 15);

        RuleConstraintService.Window window = service.resolve(constraint, now);

        assertThat(window.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(window.getEnd()).isEqualTo(now);
    }

    @Test
    void rejectUnsupportedWindowType() {
        RuleConstraint constraint = new RuleConstraint();
        constraint.setWindowType("unknown");
        constraint.setWindowValue(1);

        assertThatThrownBy(() -> service.resolve(constraint,
                LocalDateTime.of(2026, 10, 1, 0, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的规则窗口类型");
    }
}
