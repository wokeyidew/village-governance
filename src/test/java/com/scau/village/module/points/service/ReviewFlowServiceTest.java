package com.scau.village.module.points.service;

import com.scau.village.module.points.entity.RuleReview;
import com.scau.village.module.points.mapper.RuleReviewMapper;
import com.scau.village.module.points.service.impl.ReviewFlowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/** double 审核流程服务单元测试。 */
class ReviewFlowServiceTest {

    @Mock
    private RuleReviewMapper mapper;

    private ReviewFlowServiceImpl service;
    private List<RuleReview> records;
    private long nextId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReviewFlowServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        records = new ArrayList<>();
        nextId = 1L;
        when(mapper.selectList(any())).thenAnswer(invocation -> records);
        doAnswer(invocation -> {
            RuleReview review = invocation.getArgument(0);
            review.setId(nextId++);
            records.add(review);
            return 1;
        }).when(mapper).insert(any(RuleReview.class));
        doAnswer(invocation -> 1).when(mapper).updateById(any(RuleReview.class));
    }

    @Test
    void startDoubleReviewCreatesPendingStageOne() {
        service.startDoubleReview("apply-1", 100);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getStageNo()).isEqualTo(1);
        assertThat(records.get(0).getDecision()).isEqualTo("pending");
        assertThat(records.get(0).getReviewerId()).isEqualTo(100);
    }

    @Test
    void approvingStageOneDoesNotCompleteFlow() {
        service.startDoubleReview("apply-1", 100);

        assertThat(service.completeStage("apply-1", 101, "approved", "首审通过"))
                .isFalse();
        assertThat(records.get(0).getDecision()).isEqualTo("approved");
    }

    @Test
    void approvingStageTwoCompletesFlow() {
        service.startDoubleReview("apply-1", 100);
        service.completeStage("apply-1", 101, "approved", "首审通过");

        assertThat(service.completeStage("apply-1", 102, "approved", "复审通过"))
                .isTrue();
        assertThat(records).hasSize(2);
        assertThat(records.get(1).getStageNo()).isEqualTo(2);
        assertThat(service.isFullyApproved("apply-1")).isTrue();
    }

    @Test
    void rejectingStageOneDoesNotCompleteFlow() {
        service.startDoubleReview("apply-1", 100);

        assertThat(service.completeStage("apply-1", 101, "rejected", "证据不足"))
                .isFalse();
        assertThat(service.isFullyApproved("apply-1")).isFalse();
    }

    @Test
    void fullyApprovedReflectsBothStages() {
        assertThat(service.isFullyApproved("apply-1")).isFalse();

        RuleReview stageOne = review(1, "approved");
        records.add(stageOne);
        assertThat(service.isFullyApproved("apply-1")).isFalse();

        RuleReview stageTwo = review(2, "rejected");
        records.add(stageTwo);
        assertThat(service.isFullyApproved("apply-1")).isFalse();

        stageTwo.setDecision("approved");
        assertThat(service.isFullyApproved("apply-1")).isTrue();
    }

    private RuleReview review(int stageNo, String decision) {
        RuleReview review = new RuleReview();
        review.setId(nextId++);
        review.setApplyId("apply-1");
        review.setStageNo(stageNo);
        review.setDecision(decision);
        review.setTenantId(1);
        return review;
    }
}
