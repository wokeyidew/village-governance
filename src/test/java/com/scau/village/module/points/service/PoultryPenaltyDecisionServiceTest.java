package com.scau.village.module.points.service;

import com.scau.village.module.points.service.impl.PoultryPenaltyDecisionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 家禽散养阶梯决策服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class PoultryPenaltyDecisionServiceTest {

    @Mock
    private RuleOccurrenceService ruleOccurrenceService;

    @InjectMocks
    private PoultryPenaltyDecisionServiceImpl service;

    @Test
    void firstOccurrenceUsesRule43() {
        when(ruleOccurrenceService.nextOccurrenceNo(10, "poultry_free_range")).thenReturn(1);

        assertThat(service.resolveRuleId(10)).isEqualTo(43);
    }

    @Test
    void secondOccurrenceUsesRule44() {
        when(ruleOccurrenceService.nextOccurrenceNo(10, "poultry_free_range")).thenReturn(2);

        assertThat(service.resolveRuleId(10)).isEqualTo(44);
    }

    @Test
    void thirdOccurrenceUsesRule45() {
        when(ruleOccurrenceService.nextOccurrenceNo(10, "poultry_free_range")).thenReturn(3);

        assertThat(service.resolveRuleId(10)).isEqualTo(45);
    }

    @Test
    void fourthOccurrenceStillUsesRule45() {
        when(ruleOccurrenceService.nextOccurrenceNo(10, "poultry_free_range")).thenReturn(4);

        assertThat(service.resolveRuleId(10)).isEqualTo(45);
    }

    @Test
    void cancelledOccurrenceIsExcludedByOccurrenceService() {
        // occurrence 服务只返回有效记录的下一序号，已取消记录不会推进阶梯。
        when(ruleOccurrenceService.nextOccurrenceNo(10, "poultry_free_range")).thenReturn(1);

        assertThat(service.resolveRuleId(10)).isEqualTo(43);
        verify(ruleOccurrenceService).nextOccurrenceNo(10, "poultry_free_range");
    }
}
