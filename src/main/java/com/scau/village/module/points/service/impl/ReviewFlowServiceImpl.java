package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.RuleReview;
import com.scau.village.module.points.mapper.RuleReviewMapper;
import com.scau.village.module.points.service.ReviewFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** double 规则审核流程服务实现。 */
@Service
@RequiredArgsConstructor
public class ReviewFlowServiceImpl extends ServiceImpl<RuleReviewMapper, RuleReview>
        implements ReviewFlowService {

    private static final int TOTAL_STAGES = 2;

    @Override
    @Transactional
    public void startDoubleReview(String applyId, int firstReviewerId) {
        if (applyId == null || applyId.trim().isEmpty()) {
            throw new BusinessException("审核申请ID不能为空");
        }
        if (!listByApplyId(applyId).isEmpty()) {
            return;
        }
        RuleReview review = new RuleReview();
        review.setApplyId(applyId);
        review.setStageNo(1);
        review.setReviewerId(firstReviewerId);
        review.setDecision("pending");
        review.setReviewTime(LocalDateTime.now());
        review.setTenantId(UserContext.getCurrentTenantId());
        save(review);
    }

    @Override
    @Transactional
    public boolean completeStage(String applyId, int reviewerId, String decision, String remark) {
        if (!"approved".equals(decision) && !"rejected".equals(decision)) {
            throw new BusinessException("审核决定必须为 approved 或 rejected");
        }
        List<RuleReview> reviews = listByApplyId(applyId);
        if (reviews.isEmpty()) {
            throw new BusinessException("双审记录不存在");
        }

        RuleReview current = reviews.stream()
                .filter(review -> "pending".equals(review.getDecision()))
                .findFirst().orElse(null);
        if (current == null) {
            int nextStage = reviews.stream().mapToInt(RuleReview::getStageNo).max().orElse(0) + 1;
            if (nextStage > TOTAL_STAGES) {
                return isFullyApproved(applyId);
            }
            current = new RuleReview();
            current.setApplyId(applyId);
            current.setStageNo(nextStage);
            current.setTenantId(UserContext.getCurrentTenantId());
        }
        current.setReviewerId(reviewerId);
        current.setDecision(decision);
        current.setRemark(remark);
        current.setReviewTime(LocalDateTime.now());
        if (current.getId() == null) {
            save(current);
        } else {
            updateById(current);
        }
        return "approved".equals(decision) && isFullyApproved(applyId);
    }

    @Override
    public boolean isFullyApproved(String applyId) {
        List<RuleReview> reviews = listByApplyId(applyId);
        return reviews.size() >= TOTAL_STAGES
                && reviews.stream().filter(review -> review.getStageNo() != null
                && review.getStageNo() <= TOTAL_STAGES)
                .count() >= TOTAL_STAGES
                && reviews.stream().filter(review -> review.getStageNo() != null
                && review.getStageNo() <= TOTAL_STAGES)
                .allMatch(review -> "approved".equals(review.getDecision()));
    }

    private List<RuleReview> listByApplyId(String applyId) {
        LambdaQueryWrapper<RuleReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleReview::getTenantId, UserContext.getCurrentTenantId())
                .eq(RuleReview::getApplyId, applyId)
                .orderByAsc(RuleReview::getStageNo);
        return list(wrapper);
    }
}
