package com.scau.village.module.points.service;

/** 家禽散养阶梯扣分决策服务。 */
public interface PoultryPenaltyDecisionService {

    /** 根据用户历史有效发生次数决定下一次适用的阶梯（3 表示第三次及以上）。 */
    int resolveStep(int userId);

    /** 根据下一次阶梯返回应使用的规则 ID。 */
    int resolveRuleId(int userId);
}
