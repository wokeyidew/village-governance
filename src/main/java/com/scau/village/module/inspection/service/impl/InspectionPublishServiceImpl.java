package com.scau.village.module.inspection.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.inspection.entity.InspectionPublish;
import com.scau.village.module.inspection.mapper.InspectionPublishMapper;
import com.scau.village.module.inspection.service.InspectionPublishService;
import com.scau.village.module.inspection.vo.InspectionResultVO;
import com.scau.village.module.points.entity.InspectionBatch;       // ✅ 修正导入
import com.scau.village.module.points.mapper.InspectionBatchMapper; // ✅ 修正导入
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评比发布服务实现类
 *
 * @author system
 * @since 2026-07-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionPublishServiceImpl
        extends ServiceImpl<InspectionPublishMapper, InspectionPublish>
        implements InspectionPublishService {

    private final InspectionBatchMapper inspectionBatchMapper;    // ✅ 来自 points 包
    private final PointsApplyMapper pointsApplyMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public InspectionPublish publish(Long batchId, Integer tenantId, Integer publisherId, String remark) {
        // 1. 校验批次是否存在
        InspectionBatch batch = inspectionBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("检查批次不存在");
        }
        if (!batch.getTenantId().equals(tenantId.longValue())) {
            throw new BusinessException("无权操作此批次");
        }

        // 2. 查询该批次所有管理员评分记录（source_type='admin'）
        LambdaQueryWrapper<PointsApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsApply::getInspectionBatchId, batchId)
                .eq(PointsApply::getSourceType, "admin")
                .eq(PointsApply::getStatus, "approved");
        List<PointsApply> applyList = pointsApplyMapper.selectList(wrapper);
        if (applyList == null || applyList.isEmpty()) {
            throw new BusinessException("该批次暂无评分记录，无法发布");
        }

        // 3. 获取所有关联的规则ID，批量查询规则
        Set<Integer> ruleIds = applyList.stream()
                .map(PointsApply::getRuleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, PointsRule> ruleMap = new HashMap<>();
        if (!ruleIds.isEmpty()) {
            List<PointsRule> rules = pointsRuleMapper.selectBatchIds(ruleIds);
            for (PointsRule rule : rules) {
                ruleMap.put(rule.getId(), rule);
            }
        }

        // 4. 获取所有用户ID，批量查询用户信息
        Set<Integer> userIds = applyList.stream()
                .map(PointsApply::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        // 5. 按用户分组，构建快照数据
        Map<Integer, List<PointsApply>> userApplyMap = applyList.stream()
                .collect(Collectors.groupingBy(PointsApply::getUserId));

        JSONArray householdsArray = new JSONArray();
        for (Map.Entry<Integer, List<PointsApply>> entry : userApplyMap.entrySet()) {
            Integer userId = entry.getKey();
            List<PointsApply> userApplies = entry.getValue();

            User user = userMap.get(userId);
            String userName = (user != null) ? user.getRealName() : "未知用户";

            int totalScore = 0;
            JSONArray detailsArray = new JSONArray();

            for (PointsApply apply : userApplies) {
                PointsRule rule = ruleMap.get(apply.getRuleId());
                if (rule == null) {
                    continue;
                }
                int score = rule.getPoints();
                totalScore += score;

                JSONObject detail = new JSONObject();
                detail.put("ruleId", rule.getId());
                detail.put("ruleName", rule.getRuleName());
                detail.put("category", rule.getCategory());
                detail.put("score", score);
                detail.put("isBonus", score >= 0);
                detail.put("description", apply.getDescription() != null ? apply.getDescription() : "");
                detailsArray.add(detail);
            }

            JSONObject household = new JSONObject();
            household.put("userId", userId);
            household.put("userName", userName);
            household.put("totalScore", totalScore);
            household.put("details", detailsArray);
            householdsArray.add(household);
        }

        // 6. 构建完整快照JSON
        JSONObject resultJson = new JSONObject();
        resultJson.put("batchName", batch.getBatchName());
        resultJson.put("inspectionDate", batch.getInspectionDate().toString());
        resultJson.put("households", householdsArray);

        // 7. 保存或更新发布记录
        InspectionPublish publish = getPublishByBatchId(batchId);
        if (publish == null) {
            publish = new InspectionPublish();
            publish.setBatchId(batchId);
            publish.setTenantId(tenantId);
        }
        publish.setStatus("published");
        publish.setResultJson(resultJson.toJSONString());
        publish.setPublishedAt(LocalDateTime.now());
        publish.setUpdatedAt(LocalDateTime.now());

        if (publish.getId() == null) {
            publish.setCreatedAt(LocalDateTime.now());
            save(publish);
        } else {
            updateById(publish);
        }

        log.info("评比结果发布成功，batchId={}, publisherId={}", batchId, publisherId);
        return publish;
    }

    @Override
    @Transactional
    public boolean unpublish(Long batchId, Integer tenantId) {
        InspectionPublish publish = getPublishByBatchId(batchId);
        if (publish == null) {
            throw new BusinessException("该批次尚未发布");
        }
        if (!publish.getTenantId().equals(tenantId)) {
            throw new BusinessException("无权操作此批次");
        }
        publish.setStatus("draft");
        publish.setPublishedAt(null);
        publish.setUpdatedAt(LocalDateTime.now());
        return updateById(publish);
    }

    @Override
    public boolean isPublished(Long batchId) {
        InspectionPublish publish = getPublishByBatchId(batchId);
        return publish != null && "published".equals(publish.getStatus());
    }

    @Override
    public InspectionPublish getPublishByBatchId(Long batchId) {
        return baseMapper.selectByBatchId(batchId);
    }

    @Override
    public InspectionResultVO getResultForUser(Long batchId, Integer userId) {
        // 1. 检查是否已发布
        InspectionPublish publish = baseMapper.selectPublishedByBatchId(batchId);
        if (publish == null) {
            throw new BusinessException("评比结果尚未发布");
        }

        // 2. 解析快照
        String jsonStr = publish.getResultJson();
        if (jsonStr == null || jsonStr.isEmpty()) {
            throw new BusinessException("快照数据为空");
        }
        JSONObject resultJson = JSON.parseObject(jsonStr);

        // 3. 查找当前用户的数据
        JSONArray households = resultJson.getJSONArray("households");
        if (households == null || households.isEmpty()) {
            throw new BusinessException("该批次暂无数据");
        }

        JSONObject userData = null;
        for (int i = 0; i < households.size(); i++) {
            JSONObject h = households.getJSONObject(i);
            if (h.getInteger("userId").equals(userId)) {
                userData = h;
                break;
            }
        }
        if (userData == null) {
            throw new BusinessException("未找到您的评比结果");
        }

        // 4. 构建返回VO
        InspectionResultVO vo = new InspectionResultVO();
        vo.setBatchId(batchId);
        vo.setBatchName(resultJson.getString("batchName"));
        vo.setInspectionDate(java.time.LocalDate.parse(resultJson.getString("inspectionDate")));
        vo.setPublishedAt(publish.getPublishedAt());
        vo.setIsPublished(true);
        vo.setUserId(userId);
        vo.setUserName(userData.getString("userName"));
        vo.setTotalScore(userData.getInteger("totalScore"));

        // 统计加分和扣分
        JSONArray details = userData.getJSONArray("details");
        int totalBonus = 0;
        int totalPenalty = 0;
        List<InspectionResultVO.ScoreDetail> detailList = new ArrayList<>();
        if (details != null) {
            for (int i = 0; i < details.size(); i++) {
                JSONObject d = details.getJSONObject(i);
                int score = d.getInteger("score");
                if (score > 0) {
                    totalBonus += score;
                } else if (score < 0) {
                    totalPenalty += score; // 负数，直接累加
                }
                InspectionResultVO.ScoreDetail sd = new InspectionResultVO.ScoreDetail();
                sd.setRuleId(d.getInteger("ruleId"));
                sd.setRuleName(d.getString("ruleName"));
                sd.setCategory(d.getString("category"));
                sd.setScore(score);
                sd.setIsBonus(score >= 0);
                sd.setDescription(d.getString("description"));
                detailList.add(sd);
            }
        }
        vo.setTotalBonus(totalBonus);
        vo.setTotalPenalty(totalPenalty);
        vo.setDetails(detailList);

        return vo;
    }

    @Override
    public List<InspectionPublish> getPublishedBatchList(Integer tenantId) {
        return baseMapper.selectPublishedByTenantId(tenantId);
    }

    @Override
    public InspectionPublish getFullPublishDetail(Long batchId, Integer tenantId) {
        InspectionPublish publish = baseMapper.selectPublishedByBatchId(batchId);
        if (publish == null) {
            throw new BusinessException("该批次尚未发布");
        }
        if (!publish.getTenantId().equals(tenantId)) {
            throw new BusinessException("无权查看此批次");
        }
        return publish;
    }
}