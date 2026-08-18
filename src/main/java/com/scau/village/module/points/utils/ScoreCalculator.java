// 文件路径: src/main/java/com/scau/village/module/points/utils/ScoreCalculator.java
package com.scau.village.module.points.utils;

import com.alibaba.fastjson.JSON;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.PointsRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分计算工具类
 * 提供规则验证、总分计算、明细JSON生成等静态方法
 *
 * @author system
 * @since 2026-07-16
 */
@Slf4j
public class ScoreCalculator {

    /**
     * 计算规则列表的总得分（累加所有规则的 points 字段）
     *
     * @param rules 规则列表（需确保非空）
     * @return 总得分（正数加分，负数扣分）
     */
    public static int calculateTotalScore(List<PointsRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return 0;
        }
        return rules.stream().mapToInt(PointsRule::getPoints).sum();
    }

    /**
     * 验证规则列表是否全部有效（存在且状态为启用）
     *
     * @param rules 规则列表
     * @throws BusinessException 如果规则列表为空或存在无效规则
     */
    public static void validateRules(List<PointsRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            throw new BusinessException("规则列表为空，请至少选择一条规则");
        }
        for (PointsRule rule : rules) {
            if (rule == null) {
                throw new BusinessException("存在无效规则（规则对象为空）");
            }
            if (rule.getStatus() == null || rule.getStatus() != 1) {
                throw new BusinessException("规则[" + rule.getRuleName() + "]已禁用，不能使用");
            }
        }
    }

    /**
     * 生成得分明细JSON字符串
     * 格式：[{"ruleId":1,"ruleName":"庭院整洁","score":5}, ...]
     *
     * @param rules 规则列表
     * @return JSON数组字符串，若规则为空则返回 "[]"
     */
    public static String generateDetailJson(List<PointsRule> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return "[]";
        }
        List<ScoreDetail> details = rules.stream()
                .map(rule -> new ScoreDetail(rule.getId(), rule.getRuleName(), rule.getPoints()))
                .collect(Collectors.toList());
        return JSON.toJSONString(details);
    }

    /**
     * 内部类：得分明细项
     * 用于序列化为JSON
     */
    public static class ScoreDetail {
        private Integer ruleId;
        private String ruleName;
        private Integer score;

        // 无参构造（JSON序列化需要）
        public ScoreDetail() {
        }

        public ScoreDetail(Integer ruleId, String ruleName, Integer score) {
            this.ruleId = ruleId;
            this.ruleName = ruleName;
            this.score = score;
        }

        public Integer getRuleId() {
            return ruleId;
        }

        public void setRuleId(Integer ruleId) {
            this.ruleId = ruleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }
}