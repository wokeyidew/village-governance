package com.scau.village.module.points.service;

import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.ImportantContribution;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.ImportantContributionMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.impl.ImportantContributionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/** 重要贡献认定服务单元测试。 */
class ImportantContributionServiceTest {

    @Mock
    private ImportantContributionMapper contributionMapper;

    @Mock
    private PointsRuleMapper pointsRuleMapper;

    private ImportantContributionServiceImpl service;
    private ImportantContribution stored;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ImportantContributionServiceImpl(pointsRuleMapper);
        ReflectionTestUtils.setField(service, "baseMapper", contributionMapper);
        PointsRule rule = new PointsRule();
        rule.setId(18);
        rule.setPoints(10);
        rule.setBehaviorType("important");
        rule.setRuleVersion("1.0");
        when(pointsRuleMapper.selectById(18)).thenReturn(rule);
        doAnswer(invocation -> {
            stored = invocation.getArgument(0);
            stored.setId("contribution-1");
            return 1;
        }).when(contributionMapper).insert(any(ImportantContribution.class));
        when(contributionMapper.selectById(any())).thenAnswer(invocation -> stored);
        doAnswer(invocation -> {
            stored = invocation.getArgument(0);
            return 1;
        }).when(contributionMapper).updateById(any(ImportantContribution.class));
    }

    @Test
    void submitCreatesPendingCareContribution() {
        ImportantContribution contribution = service.submit(
                7, 18, "长期照顾老人", "photo.jpg", "care-ref-1");

        assertThat(contribution.getStatus()).isEqualTo(ImportantContribution.STATUS_PENDING);
        assertThat(contribution.getContributionType()).isEqualTo("care");
        assertThat(contribution.getRuleId()).isEqualTo(18);
        assertThat(contribution.getSourceRef()).isEqualTo("care-ref-1");
    }

    @Test
    void pendingContributionCannotBeUsed() {
        service.submit(7, 18, "长期照顾老人", null, null);

        assertThatThrownBy(() -> service.requireApproved(7, 18, "contribution-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("重要贡献认定未通过或不存在");
    }

    @Test
    void approvedContributionCanBeUsed() {
        service.submit(7, 18, "长期照顾老人", null, null);
        service.approve("contribution-1", 99, "认定通过");

        assertThat(service.requireApproved(7, 18, "contribution-1").getStatus())
                .isEqualTo(ImportantContribution.STATUS_APPROVED);
    }

    @Test
    void mismatchedRuleCannotBeUsed() {
        service.submit(7, 18, "长期照顾老人", null, null);
        service.approve("contribution-1", 99, null);

        assertThatThrownBy(() -> service.requireApproved(7, 21, "contribution-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("重要贡献认定未通过或不存在");
    }

    @Test
    void mismatchedUserCannotBeUsed() {
        service.submit(7, 18, "长期照顾老人", null, null);
        service.approve("contribution-1", 99, null);

        assertThatThrownBy(() -> service.requireApproved(8, 18, "contribution-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("重要贡献认定未通过或不存在");
    }
}
