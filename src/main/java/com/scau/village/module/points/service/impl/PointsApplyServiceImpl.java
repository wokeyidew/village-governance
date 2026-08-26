// 文件路径: src/main/java/com/scau/village/module/points/service/impl/PointsApplyServiceImpl.java
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

        PointsApply apply = new PointsApply();
        apply.setTenantId(tenantId);
        apply.setUserId(userId.intValue());
        apply.setRuleId(rule.getId());
        apply.setDescription(dto.getDescription());
        apply.setImages(dto.getImages());
        apply.setStatus("pending");
        apply.setSourceType("user");
        apply.setCreateTime(LocalDateTime.now());
        save(apply);
    }

    @Override
    @Transactional
    public void approve(Integer applyId, Long auditorId, Boolean approved, String remark) {
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

            user.setPoints(user.getPoints() + rule.getPoints());
            userMapper.updateById(user);

            PointsFlow flow = new PointsFlow();
            flow.setUserId(user.getId());
            flow.setChangeAmount(rule.getPoints());
            flow.setSourceType("apply");
            flow.setSourceId(apply.getId());
            flow.setRemark("积分申报审核通过:" + rule.getRuleName());
            flow.setCreateTime(LocalDateTime.now());
            flowMapper.insert(flow);

            operationLogService.log(auditorId, "POINTS_AUDIT",
                    String.format("审核通过积分申报 ID:%d，规则:%s，增加积分:%d", applyId, rule.getRuleName(), rule.getPoints()));
        } else {
            apply.setStatus("rejected");
            operationLogService.log(auditorId, "POINTS_AUDIT",
                    String.format("驳回积分申报 ID:%d，原因:%s", applyId, remark));
        }
        updateById(apply);
    }

    // ==================== 新增管理员评分方法 ====================
    @Override
    @Transactional
    public void saveAdminScore(ScoreSubmitDto dto, Integer inspectorId, Integer tenantId) {
        // 1. 校验批次存在
        InspectionBatch batch = inspectionBatchService.getById(dto.getBatchId());
        if (batch == null) {
            throw new BusinessException("检查批次不存在");
        }

        // 2. 校验用户存在
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 校验规则列表有效性，并计算总得分
        List<Integer> ruleIds = dto.getRules();
        if (ruleIds == null || ruleIds.isEmpty()) {
            throw new BusinessException("至少选择一条评分规则");
        }
        List<PointsRule> rules = ruleMapper.selectBatchIds(ruleIds);
        if (rules.size() != ruleIds.size()) {
            throw new BusinessException("部分规则不存在或已被删除");
        }

        boolean hasPenaltyRule = rules.stream().anyMatch(rule -> rule.getPoints() < 0);

        if (hasPenaltyRule) {
            List<String> images = dto.getImages();
            if (images == null || images.isEmpty()) {
                throw new BusinessException("扣分项必须上传现场照片证据");
            }
        }

        int totalScore = 0;
        List<ScoreDetail> detailList = new ArrayList<>();
        List<PointsRule> penaltyRules = new ArrayList<>();
        for (PointsRule rule : rules) {
            if (rule.getStatus() != 1) {
                throw new BusinessException("规则[" + rule.getRuleName() + "]已禁用，不能使用");
            }
            totalScore += rule.getPoints();
            if (rule.getPoints() < 0) {
                penaltyRules.add(rule);
            }
            ScoreDetail detail = new ScoreDetail();
            detail.setRuleId(rule.getId());
            detail.setRuleName(rule.getRuleName());
            detail.setScore(rule.getPoints());
            detailList.add(detail);
        }

        List<String> watermarkedImageUrls = null;
        if (hasPenaltyRule && dto.getImages() != null && !dto.getImages().isEmpty()) {
            watermarkedImageUrls = dto.getImages();
            log.info("扣分规则照片数量：{}", watermarkedImageUrls.size());
        }

        LocalDate inspectionDate = batch.getInspectionDate();

        for (PointsRule rule : rules) {
            PointsApply apply = new PointsApply();
            apply.setTenantId(tenantId);
            apply.setUserId(dto.getUserId());
            apply.setRuleId(rule.getId());
            apply.setDescription(dto.getDescription() != null ? dto.getDescription() : rule.getRuleName() + "（现场评分）");
            if (hasPenaltyRule && watermarkedImageUrls != null && !watermarkedImageUrls.isEmpty()) {
                apply.setImages(String.join(",", watermarkedImageUrls));
            } else {
                apply.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
            }
            apply.setStatus("approved");
            apply.setAuditorId(inspectorId);
            apply.setAuditRemark("管理员现场评分");
            apply.setAuditTime(LocalDateTime.now());
            apply.setSourceType("admin");
            apply.setInspectorId(inspectorId);
            apply.setInspectionBatchId(dto.getBatchId());
            apply.setInspectionDate(inspectionDate);
            apply.setHasEvidence(hasPenaltyRule ? 1 : 0);
            apply.setCreateTime(LocalDateTime.now());
            save(apply);

            PointsFlow flow = new PointsFlow();
            flow.setUserId(user.getId());
            flow.setChangeAmount(rule.getPoints());
            flow.setSourceType("admin_inspection");
            flow.setSourceId(apply.getId());
            flow.setRemark("管理员现场评分：" + rule.getRuleName() + "，批次：" + batch.getBatchName());
            flow.setCreateTime(LocalDateTime.now());
            flowMapper.insert(flow);

            // 如果是扣分规则，保存证据和整改任务
            if (rule.getPoints() < 0) {
                // ========== 保存证据（增加详细日志和异常捕获） ==========
                if (watermarkedImageUrls != null && !watermarkedImageUrls.isEmpty()) {
                    String photoUrls = String.join(",", watermarkedImageUrls);
                    log.info("【证据保存】开始保存证据，applyId={}, 照片数量={}, 照片URL={}",
                            apply.getId(), watermarkedImageUrls.size(), photoUrls);

                    try {
                        ScoreEvidence evidence = new ScoreEvidence();
                        evidence.setApplyId(apply.getId().longValue());
                        evidence.setPhotoUrls(photoUrls);
                        evidence.setPhotoCount(watermarkedImageUrls.size());
                        evidence.setLocation("");
                        evidence.setInspectorId(inspectorId.longValue());
                        evidence.setBatchId(dto.getBatchId());
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
                        // 捕获异常，不影响主流程（只记录日志）
                        log.error("【证据保存】❌ 保存证据失败，applyId={}, error={}",
                                apply.getId(), e.getMessage(), e);
                    }
                } else {
                    // 扣分项但没有照片，记录警告日志
                    log.warn("【证据保存】⚠️ 扣分项但没有照片，applyId={}, rule={}, 请检查前端是否上传图片",
                            apply.getId(), rule.getRuleName());
                }

                // ========== 自动创建整改任务 ==========
                try {
                    rectificationTaskService.createTask(
                            apply.getId().longValue(),
                            dto.getUserId().longValue(),
                            dto.getBatchId(),
                            rule.getRuleName(),
                            "请按照要求进行整改，整改完成后拍照上传。",
                            LocalDateTime.now().plusDays(7),
                            watermarkedImageUrls != null ? String.join(",", watermarkedImageUrls) : null,
                            inspectorId.longValue()
                    );
                    log.info("自动创建整改任务成功，applyId={}", apply.getId());
                } catch (Exception e) {
                    log.error("创建整改任务失败，applyId={}, error={}", apply.getId(), e.getMessage(), e);
                }
            }
        }

        // 7. 更新用户总积分（累加所有规则得分）
        user.setPoints(user.getPoints() + totalScore);
        userMapper.updateById(user);

        // 8. 更新/插入户汇总表
        String detailJson = JSON.toJSONString(detailList);
        inspectionHouseholdService.saveOrUpdateSummary(
                dto.getBatchId(),
                dto.getUserId(),
                totalScore,
                detailJson,
                inspectorId,
                dto.getDescription()
        );

        // 9. 记录操作日志
        operationLogService.log(inspectorId.longValue(), "INSPECTION_SCORE",
                String.format("批次[%s]为用户[%s](ID:%s)评分，总得分:%d，规则数:%d",
                        batch.getBatchName(), user.getPhone(), user.getId(), totalScore, rules.size()));
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