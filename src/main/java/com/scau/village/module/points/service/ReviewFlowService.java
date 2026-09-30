package com.scau.village.module.points.service;

/** double 规则审核流程服务。 */
public interface ReviewFlowService {

    /** 创建第一阶段待审核记录。 */
    void startDoubleReview(String applyId, int firstReviewerId);

    /** 完成当前阶段审核，返回是否所有阶段均已通过。 */
    boolean completeStage(String applyId, int reviewerId, String decision, String remark);

    /** 判断申请是否已经完成全部审核阶段并通过。 */
    boolean isFullyApproved(String applyId);
}
