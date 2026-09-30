package com.scau.village.module.points.service;

/** 规则月度频次配额服务。 */
public interface MonthlyQuotaService {

    /** 检查当前窗口是否仍有可用配额。 */
    void assertAvailable(int userId, int ruleId);

    /** 返回当前窗口剩余次数，-1 表示不限。 */
    int getRemaining(int userId, int ruleId);
}
