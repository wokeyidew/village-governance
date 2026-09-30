package com.scau.village.module.points.service;

import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.RuleConstraint;
import com.scau.village.module.points.service.impl.MonthlyQuotaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 月度频次配额服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class MonthlyQuotaServiceTest {

    @Mock
    private RuleConstraintService ruleConstraintService;

    private MonthlyQuotaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MonthlyQuotaServiceImpl(ruleConstraintService);
    }

    @Test
    void zeroMaxTimesMeansUnlimited() {
        RuleConstraint constraint = constraint(0);
        when(ruleConstraintService.getEffective(eq(12), any())).thenReturn(constraint);

        service.assertAvailable(1, 12);

        verify(ruleConstraintService, never()).resolve(any(), any());
        assertThat(service.getRemaining(1, 12)).isEqualTo(-1);
    }

    @Test
    void threeApprovedApplicationsStillHaveOneSlot() {
        RuleConstraint constraint = constraint(4);
        when(ruleConstraintService.getEffective(eq(12), any())).thenReturn(constraint);
        when(ruleConstraintService.resolve(any(), any())).thenReturn(window());
        when(ruleConstraintService.countInWindow(eq(1), eq(12), any())).thenReturn(3);

        service.assertAvailable(1, 12);

        assertThat(service.getRemaining(1, 12)).isEqualTo(1);
    }

    @Test
    void fourApprovedApplicationsAreRejected() {
        RuleConstraint constraint = constraint(4);
        when(ruleConstraintService.getEffective(eq(12), any())).thenReturn(constraint);
        when(ruleConstraintService.resolve(any(), any())).thenReturn(window());
        when(ruleConstraintService.countInWindow(eq(1), eq(12), any())).thenReturn(4);

        assertThatThrownBy(() -> service.assertAvailable(1, 12))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该规则本月已达上限（4次）");
    }

    @Test
    void naturalMonthWindowIsPassedToCounter() {
        RuleConstraint constraint = constraint(4);
        when(ruleConstraintService.getEffective(eq(1), any())).thenReturn(constraint);
        when(ruleConstraintService.resolve(any(), any())).thenReturn(window());
        when(ruleConstraintService.countInWindow(eq(1), eq(1), any())).thenReturn(0);

        service.assertAvailable(1, 1);

        ArgumentCaptor<RuleConstraintService.Window> captor =
                ArgumentCaptor.forClass(RuleConstraintService.Window.class);
        verify(ruleConstraintService).countInWindow(eq(1), eq(1), captor.capture());
        assertThat(captor.getValue().getStart().getDayOfMonth()).isEqualTo(1);
    }

    private RuleConstraint constraint(int maxTimes) {
        RuleConstraint constraint = new RuleConstraint();
        constraint.setMaxTimes(maxTimes);
        constraint.setWindowType("natural_month");
        constraint.setWindowValue(1);
        return constraint;
    }

    private RuleConstraintService.Window window() {
        LocalDateTime now = LocalDateTime.now();
        return new RuleConstraintService.Window(now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now);
    }
}
