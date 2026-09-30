package com.scau.village.module.points.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.utils.WatermarkUtils;
import com.scau.village.module.log.service.OperationLogService;
import com.scau.village.module.points.dto.ApplyDto;
import com.scau.village.module.points.dto.ScoreSubmitDto;
import com.scau.village.module.points.entity.*;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.InspectionBatchService;
import com.scau.village.module.points.service.InspectionHouseholdService;
import com.scau.village.module.points.service.PointsApplyService;
import com.scau.village.module.points.service.PoultryPenaltyDecisionService;
import com.scau.village.module.points.service.RuleConstraintService;
import com.scau.village.module.points.service.RuleObservationEventService;
import com.scau.village.module.points.service.RuleOccurrenceService;
import com.scau.village.module.points.service.ScoreEvidenceService;
import com.scau.village.module.rectification.service.RectificationTaskService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsApplyServiceImpl extends ServiceImpl<PointsApplyMapper, PointsApply> implements PointsApplyService {

    private final PointsRuleMapper ruleMapper;
    private final UserMapper userMapper;
    private final PointsFlowMapper flowMapper;
    private final OperationLogService operationLogService;
    private final InspectionBatchService inspectionBatchService;
    private final InspectionHouseholdService inspectionHouseholdService;
    private final ScoreEvidenceService scoreEvidenceService;
    private final RectificationTaskService rectificationTaskService;
    private final WatermarkUtils watermarkUtils;
    private final RuleConstraintService ruleConstraintService;
    private final RuleObservationEventService ruleObservationEventService;
    private final PoultryPenaltyDecisionService poultryPenaltyDecisionService;
    private final RuleOccurrenceService ruleOccurrenceService;

    // ==================== 原有村民申报方法 ====================
    @Override
    @Transactional
    public void submitApply(ApplyDto dto, Long userId, Integer tenantId) {
        PointsRule rule = ruleMapper.selectById(dto.getRuleId());
        if (rule == null || rule.getStatus() != 1) {
            throw new BusinessException("积分规则不存在或已禁用");
        }

        // 每日上限校验
        if (rule.getMaxTimesPerDay() != null && rule.getMaxTimesPerDay() > 0) {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            long countToday = lambdaQuery()
                    .eq(PointsApply::getUserId, userId)
                    .eq(PointsApply::getRuleId, rule.getId())
                    .eq(PointsApply::getStatus, "approved")
                    .between(PointsApply::getCreateTime, startOfDay, endOfDay)
                    .count();
            if (countToday >= rule.getMaxTimesPerDay()) {
                throw new BusinessException("今日申报次数已达上限");
            }
        }

        LocalDateTime applyTime = LocalDateTime.now();
        if (rule.getId() != null && rule.getId() == 5) {
            validateRuleFiveWindow(userId.intValue(), applyTime);
        }

        PointsApply apply = new PointsApply();
        apply.setTenantId(tenantId);
        apply.setUserId(userId.intValue());
        apply.setRuleId(rule.getId());
        apply.setDescription(dto.getDescription());
        apply.setImages(dto.getImages());
        apply.setStatus("pending");
        apply.setSourceType("user");
        apply.setCreateTime(applyTime);
        save(apply);
    }

    @Override
    @Transactional
    public void approve(String applyId, Long auditorId, Boolean approved, String remark) {
        PointsApply apply = getById(applyId);
        if (apply == null || !"pending".equals(apply.getStatus())) {
            throw new BusinessException("申报记录不存在或已处理");
        }
        if (!"user".equals(apply.getSourceType())) {
            throw new BusinessException("该记录非村民申报，无需审核");
        }

        apply.setAuditorId(auditorId.intValue());
        apply.setAuditRemark(remark);
        apply.setAuditTime(LocalDateTime.now());

        if (approved) {
            apply.setStatus("approved");
            PointsRule rule = ruleMapper.selectById(apply.getRuleId());
            User user = userMapper.selectById(apply.getUserId());

            // 更新积分（同时更新三个字段）
            int changeAmount = rule.getPoints();
            user.setPoints(user.getPoints() + changeAmount);
            // total_earned_points 只增不减（只有加分时才累加）
            if (changeAmount > 0) {
                user.setTotalEarnedPoints(user.getTotalEarnedPoints() + changeAmount);
            }
            user.setAvailablePoints(user.getAvailablePoints() + changeAmount);
            userMapper.updateById(user);

            PointsFlow flow = new PointsFlow();
            flow.setUserId(user.getId());
            flow.setChangeAmount(rule.getPoints());
            flow.setSourceType("apply");
            flow.setSourceId(apply.getId().toString());
            flow.setRemark("积分申报审核通过:" + rule.getRuleName());
            flow.setCreateTime(LocalDateTime.now());
            flowMapper.insert(flow);

            if (rule.getId() != null && rule.getId() == 5) {
                ruleObservationEventService.record(apply.getUserId(), 5, "classified",
                        apply.getAuditTime(), apply.getId() == null ? null : apply.getId().toString());
            }

            operationLogService.log(auditorId, "POINTS_AUDIT",
                    String.format("审核通过积分申报 ID:%s，规则:%s，增加积分:%d", applyId, rule.getRuleName(), rule.getPoints()));
        } else {
            apply.setStatus("rejected");
            operationLogService.log(auditorId, "POINTS_AUDIT",
                    String.format("驳回积分申报 ID:%d，原因:%s", applyId, remark));
        }
        updateById(apply);
    }

    // ==================== 管理员评分方法（核心修复） ====================
    @Override
    @Transactional
    public void saveAdminScore(ScoreSubmitDto dto, Integer inspectorId, Integer tenantId) {
        log.info("【评分提交】开始处理，batchId={}, userId={}, rules={}, images={}",
                dto.getBatchId(), dto.getUserId(), dto.getRules(), dto.getImages());

        Long batchId;
        try {
            batchId = Long.parseLong(dto.getBatchId());
        } catch (NumberFormatException e) {
            log.error("【评分提交】batchId 格式错误: {}", dto.getBatchId(), e);
            throw new BusinessException("批次ID格式错误");
        }

        // 1. 校验批次存在
        InspectionBatch batch = inspectionBatchService.getById(batchId);
        if (batch == null) {
            log.warn("【评分提交】批次不存在，batchId={}", batchId);
            throw new BusinessException("检查批次不存在");
        }
        log.info("【评分提交】批次校验通过，batchName={}", batch.getBatchName());

        // 2. 校验用户存在
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            log.warn("【评分提交】用户不存在，userId={}", dto.getUserId());
            throw new BusinessException("用户不存在");
        }
        log.info("【评分提交】用户校验通过，userName={}", user.getRealName());

        // 3. 校验规则列表有效性
        List<Integer> ruleIds = dto.getRules();
        if (ruleIds == null || ruleIds.isEmpty()) {
            throw new BusinessException("至少选择一条评分规则");
        }
        List<PointsRule> rules = ruleMapper.selectBatchIds(ruleIds);
        if (rules.size() != ruleIds.size()) {
            log.warn("【评分提交】部分规则不存在，请求规则数={}, 实际查询到={}", ruleIds.size(), rules.size());
            throw new BusinessException("部分规则不存在或已被删除");
        }

        // 检查是否包含扣分规则
        // 家禽散养规则必须根据历史发生次数确定阶梯，不能由调用方直接选择任意规则 ID。
        Integer poultryExpectedRuleId = null;
        Integer poultryExpectedStep = null;
        Integer poultryOccurrenceNo = null;
        for (PointsRule rule : rules) {
            if ("poultry_free_range".equals(rule.getRuleFamilyCode())) {
                if (poultryExpectedRuleId == null) {
                    poultryExpectedRuleId = poultryPenaltyDecisionService.resolveRuleId(dto.getUserId());
                    poultryExpectedStep = poultryExpectedRuleId == 43 ? 1
                            : poultryExpectedRuleId == 44 ? 2 : 3;
                    poultryOccurrenceNo = ruleOccurrenceService.nextOccurrenceNo(
                            dto.getUserId(), "poultry_free_range");
                }
                if (!Integer.valueOf(rule.getId()).equals(poultryExpectedRuleId)) {
                    throw new BusinessException("家禽散养阶梯与历史发现次数不一致，应为第"
                            + poultryExpectedStep + "次");
                }
            }
        }

        boolean hasPenaltyRule = rules.stream().anyMatch(rule -> rule.getPoints() < 0);

        // 扣分项必须上传照片
        if (hasPenaltyRule) {
            List<String> images = dto.getImages();
            if (images == null || images.isEmpty()) {
                log.warn("【评分提交】扣分项缺少照片证据，rules={}", ruleIds);
                throw new BusinessException("扣分项必须上传现场照片证据");
            }
            log.info("【评分提交】扣分项照片数量：{}", images.size());
        }

        // 计算总得分 和 正分总和（用于 total_earned_points）
        int totalScore = 0;
        int positiveScoreSum = 0;
        List<ScoreDetail> detailList = new ArrayList<>();
        for (PointsRule rule : rules) {
            if (rule.getStatus() != 1) {
                throw new BusinessException("规则[" + rule.getRuleName() + "]已禁用，不能使用");
            }
            int rulePoints = rule.getPoints();
            totalScore += rulePoints;
            if (rulePoints > 0) {
                positiveScoreSum += rulePoints;
            }
            ScoreDetail detail = new ScoreDetail();
            detail.setRuleId(rule.getId());
            detail.setRuleName(rule.getRuleName());
            detail.setScore(rulePoints);
            detailList.add(detail);
        }

        LocalDateTime scoreTime = LocalDateTime.now();
        if (rules.stream().anyMatch(rule -> rule.getId() != null && rule.getId() == 5)) {
            validateRuleFiveWindow(dto.getUserId(), scoreTime);
        }

        LocalDate inspectionDate = batch.getInspectionDate();

        // 遍历规则，逐条写入 points_apply 和 points_flow
        for (PointsRule rule : rules) {
            // ----- 1. 写入 points_apply -----
            PointsApply apply = new PointsApply();
            apply.setTenantId(tenantId);
            apply.setUserId(dto.getUserId());
            apply.setRuleId(rule.getId());
            apply.setDescription(dto.getDescription() != null ? dto.getDescription() : rule.getRuleName() + "（现场评分）");
            // 图片处理
            if (hasPenaltyRule && dto.getImages() != null && !dto.getImages().isEmpty()) {
                apply.setImages(String.join(",", dto.getImages()));
            } else {
                apply.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
            }
            apply.setStatus("approved");
            apply.setAuditorId(inspectorId);
            apply.setAuditRemark("管理员现场评分");
            apply.setAuditTime(LocalDateTime.now());
            apply.setSourceType("admin_inspection");
            apply.setInspectorId(inspectorId);
            apply.setInspectionBatchId(batchId);
            apply.setInspectionDate(inspectionDate);
            apply.setHasEvidence(hasPenaltyRule ? 1 : 0);
            apply.setCreateTime(LocalDateTime.now());

            save(apply);
            log.info("【评分提交】✅ 写入 points_apply 成功，applyId={}, ruleId={}, sourceType={}",
                    apply.getId(), rule.getId(), apply.getSourceType());

            if ("poultry_free_range".equals(rule.getRuleFamilyCode())) {
                ruleOccurrenceService.record(dto.getUserId(), "poultry_free_range",
                        poultryOccurrenceNo == null ? poultryExpectedStep : poultryOccurrenceNo,
                        apply.getId(), rule.getRuleVersion());
            }

            if (rule.getId() != null && rule.getId() == 5) {
                ruleObservationEventService.record(dto.getUserId(), 5, "classified",
                        scoreTime, apply.getId() == null ? null : apply.getId().toString());
            }

            // ----- 2. 写入 points_flow -----
            PointsFlow flow = new PointsFlow();
            flow.setUserId(user.getId());
            flow.setChangeAmount(rule.getPoints());
            flow.setSourceType("admin_inspection");
            flow.setSourceId(apply.getId().toString());
            flow.setRemark("管理员现场评分：" + rule.getRuleName() + "，批次：" + batch.getBatchName());
            flow.setCreateTime(LocalDateTime.now());

            flow.setBatchId(String.valueOf(batchId));
            flow.setBatchName(batch.getBatchName());
            flow.setApplyId(String.valueOf(apply.getId()));

            flowMapper.insert(flow);
            log.info("【评分提交】✅ 写入 points_flow 成功，flowId={}, sourceId={}, changeAmount={}, batchId={}, applyId={}",
                    flow.getId(), flow.getSourceId(), flow.getChangeAmount(), flow.getBatchId(), flow.getApplyId());

            // ----- 3. 如果是扣分规则，保存证据并创建整改任务 -----
            if (rule.getPoints() < 0) {
                // 保存证据
                if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                    String photoUrls = String.join(",", dto.getImages());
                    log.info("【证据保存】开始保存证据，applyId={}, 照片数量={}",
                            apply.getId(), dto.getImages().size());

                    try {
                        ScoreEvidence evidence = new ScoreEvidence();
                        evidence.setApplyId(String.valueOf(apply.getId()));
                        evidence.setPhotoUrls(photoUrls);
                        evidence.setPhotoCount(dto.getImages().size());
                        evidence.setLocation("");
                        evidence.setInspectorId(String.valueOf(inspectorId));
                        evidence.setBatchId(String.valueOf(batchId));
                        evidence.setRuleVersion("1.0");
                        evidence.setRuleName(rule.getRuleName());
                        evidence.setUserName(user.getRealName());
                        evidence.setHasWatermark(1);
                        evidence.setCreateTime(LocalDateTime.now());
                        evidence.setTenantId(tenantId);

                        scoreEvidenceService.save(evidence);
                        log.info("【证据保存】✅ 保存证据成功，applyId={}, evidenceId={}",
                                apply.getId(), evidence.getId());

                    } catch (Exception e) {
                        log.error("【证据保存】❌ 保存证据失败，applyId={}, error={}",
                                apply.getId(), e.getMessage(), e);
                    }
                } else {
                    log.warn("【证据保存】⚠️ 扣分项但没有照片，applyId={}, rule={}",
                            apply.getId(), rule.getRuleName());
                }

                // 创建整改任务
                try {
                    rectificationTaskService.createTask(
                            String.valueOf(apply.getId()),
                            dto.getUserId().longValue(),
                            String.valueOf(batchId),
                            rule.getRuleName(),
                            "请按照要求进行整改，整改完成后拍照上传。",
                            LocalDateTime.now().plusDays(7),
                            dto.getImages() != null ? String.join(",", dto.getImages()) : null,
                            String.valueOf(inspectorId)
                    );
                    log.info("【整改任务】✅ 创建整改任务成功，applyId={}", apply.getId());
                } catch (Exception e) {
                    log.error("【整改任务】❌ 创建整改任务失败，applyId={}, error={}",
                            apply.getId(), e.getMessage(), e);
                }
            }
        }

        // 7. 更新用户积分（同时更新三个字段）
        int newPoints = user.getPoints() + totalScore;
        user.setPoints(newPoints);
        if (positiveScoreSum > 0) {
            user.setTotalEarnedPoints(user.getTotalEarnedPoints() + positiveScoreSum);
        }
        user.setAvailablePoints(newPoints);
        userMapper.updateById(user);
        log.info("【评分提交】用户积分更新，userId={}, 新增积分={}, 正分总和={}, 当前总积分={}, 总获得积分={}, 可用积分={}",
                user.getId(), totalScore, positiveScoreSum, user.getPoints(),
                user.getTotalEarnedPoints(), user.getAvailablePoints());

        // 8. 更新/插入户汇总表（修复：将 batchId 和 inspectorId 转为 String）
        String detailJson = JSON.toJSONString(detailList);
        // 调用 service 保存汇总，增加日志记录结果
        InspectionHousehold household = inspectionHouseholdService.saveOrUpdateSummary(
                String.valueOf(batchId),
                dto.getUserId(),
                totalScore,
                detailJson,
                String.valueOf(inspectorId),
                dto.getDescription()
        );
        log.info("【评分提交】户汇总表更新完成，userId={}, totalScore={}, householdId={}",
                dto.getUserId(), totalScore, household != null ? household.getId() : "null");

        // 9. 记录操作日志
        operationLogService.log(inspectorId.longValue(), "INSPECTION_SCORE",
                String.format("批次[%s]为用户[%s](ID:%s)评分，总得分:%d，规则数:%d",
                        batch.getBatchName(), user.getPhone(), user.getId(), totalScore, rules.size()));

        log.info("【评分提交】✅ 全部处理完成，userId={}, 规则数={}, 总得分={}",
                dto.getUserId(), rules.size(), totalScore);
    }

    /**
     * 校验规则 #5 的滚动 30 天无混装条件。
     * 窗口由 rule_constraint 表决定，当前迁移版本为 rolling_days=30。
     */
    private void validateRuleFiveWindow(int userId, LocalDateTime now) {
        RuleConstraintService.Window window;
        com.scau.village.module.points.entity.RuleConstraint constraint =
                ruleConstraintService.getEffective(5, now);
        if (constraint == null) {
            throw new BusinessException("规则#5时间窗口约束未配置");
        }
        try {
            window = ruleConstraintService.resolve(constraint, now);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("规则#5时间窗口约束无效");
        }
        if (ruleObservationEventService.exists(userId, 5, "mixed",
                window.getStart(), window.getEnd())) {
            throw new BusinessException("连续一个月无混装条件不满足");
        }
    }

    // 内部类用于构建明细JSON
    private static class ScoreDetail {
        private Integer ruleId;
        private String ruleName;
        private Integer score;

        public Integer getRuleId() { return ruleId; }
        public void setRuleId(Integer ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }
}
