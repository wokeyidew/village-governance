package com.scau.village.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scau.village.module.dashboard.service.DashboardService;
import com.scau.village.module.dashboard.vo.DistributionVO;
import com.scau.village.module.dashboard.vo.EffectivenessVO;
import com.scau.village.module.dashboard.vo.ProblemTypeVO;
import com.scau.village.module.dashboard.vo.TrendVO;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.entity.PublishSnapshot;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.PublishSnapshotService;
import com.scau.village.module.points.vo.SnapshotVO;
import com.scau.village.module.rectification.entity.RectificationTask;
import com.scau.village.module.rectification.mapper.RectificationTaskMapper;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PointsApplyMapper pointsApplyMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final RectificationTaskMapper rectificationTaskMapper;
    private final UserMapper userMapper;
    private final PublishSnapshotService publishSnapshotService;

    // ==================== 1. 趋势数据（近6个月） ====================

    @Override
    public List<TrendVO> getTrendData(Integer tenantId) {
        if (tenantId == null) {
            return new ArrayList<>();
        }

        // 获取最近6个月的月份列表（从当前月开始往前推6个月）
        List<String> months = getLastSixMonths();

        List<TrendVO> trendList = new ArrayList<>();
        for (String month : months) {
            TrendVO vo = new TrendVO();
            vo.setMonth(month);

            // 统计该月扣分项（规则 points < 0）
            // 关联 points_apply 和 points_rule，筛选 tenant_id，按月份过滤
            // 直接用 SQL 聚合，这里使用 MyBatis-Plus 条件构造
            LambdaQueryWrapper<PointsApply> applyWrapper = new LambdaQueryWrapper<>();
            applyWrapper.eq(PointsApply::getTenantId, tenantId)
                    .eq(PointsApply::getSourceType, "admin")  // 只统计管理员评分
                    .apply("DATE_FORMAT(create_time, '%Y-%m') = {0}", month);

            List<PointsApply> applies = pointsApplyMapper.selectList(applyWrapper);
            if (applies.isEmpty()) {
                // 无数据时填0
                vo.setTotalProblems(0);
                vo.setResolvedProblems(0);
                vo.setCompletionRate(0.0);
                vo.setParticipantCount(0);
                vo.setTotalPenaltyPoints(0);
                vo.setTotalBonusPoints(0);
                vo.setNetPoints(0);
                trendList.add(vo);
                continue;
            }

            // 统计问题总数（扣分项）
            int totalProblems = 0;
            int totalPenalty = 0;
            int totalBonus = 0;
            Set<Integer> userIds = new HashSet<>();

            for (PointsApply apply : applies) {
                PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
                if (rule == null) continue;
                if (rule.getPoints() < 0) {
                    totalProblems++;
                    totalPenalty += Math.abs(rule.getPoints());
                } else {
                    totalBonus += rule.getPoints();
                }
                userIds.add(apply.getUserId());
            }

            // 统计已整改问题数（该月扣分项对应的整改任务已销项）
            // 实际上需要关联 rectification_task 表，根据 apply_id
            int resolvedProblems = 0;
            for (PointsApply apply : applies) {
                if (apply.getRuleId() != null) {
                    PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
                    if (rule != null && rule.getPoints() < 0) {
                        // 查询是否存在已销项的整改任务
                        LambdaQueryWrapper<RectificationTask> taskWrapper = new LambdaQueryWrapper<>();
                        taskWrapper.eq(RectificationTask::getApplyId, apply.getId().longValue())
                                .eq(RectificationTask::getStatus, RectificationTask.STATUS_RESOLVED);
                        Long count = rectificationTaskMapper.selectCount(taskWrapper);
                        if (count > 0) {
                            resolvedProblems++;
                        }
                    }
                }
            }

            // 参与户数（该月有积分记录的不同用户数）
            int participantCount = userIds.size();

            vo.setTotalProblems(totalProblems);
            vo.setResolvedProblems(resolvedProblems);
            vo.setCompletionRate(totalProblems == 0 ? 0.0 : (double) resolvedProblems / totalProblems * 100);
            vo.setParticipantCount(participantCount);
            vo.setTotalPenaltyPoints(totalPenalty);
            vo.setTotalBonusPoints(totalBonus);
            vo.setNetPoints(totalBonus - totalPenalty);

            trendList.add(vo);
        }

        return trendList;
    }

    // ==================== 2. 分布数据（各村组平均分） ====================

    @Override
    public List<DistributionVO> getDistributionData(Integer tenantId) {
        if (tenantId == null) {
            return new ArrayList<>();
        }

        // 获取所有村民用户（按 village_group 分组）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getTenantId, tenantId)
                .eq(User::getRole, "VILLAGER")
                .isNotNull(User::getVillageGroup)
                .ne(User::getVillageGroup, "");
        List<User> users = userMapper.selectList(userWrapper);

        if (users.isEmpty()) {
            return new ArrayList<>();
        }

        // 按村组分组
        Map<String, List<User>> groupMap = users.stream()
                .collect(Collectors.groupingBy(User::getVillageGroup));

        List<DistributionVO> result = new ArrayList<>();
        for (Map.Entry<String, List<User>> entry : groupMap.entrySet()) {
            String groupName = entry.getKey();
            List<User> groupUsers = entry.getValue();

            DistributionVO vo = new DistributionVO();
            vo.setVillageGroup(groupName);
            vo.setParticipantCount(groupUsers.size());

            // 计算该组所有用户的积分总和
            int totalScore = 0;
            int minScore = Integer.MAX_VALUE;
            int maxScore = Integer.MIN_VALUE;
            int problemCount = 0;
            int resolvedCount = 0;

            for (User user : groupUsers) {
                totalScore += user.getPoints();
                if (user.getPoints() < minScore) minScore = user.getPoints();
                if (user.getPoints() > maxScore) maxScore = user.getPoints();

                // 统计扣分项（可优化，这里简单处理）
                // 实际上需要查 points_apply，但为了性能，暂不深入
            }

            double avg = groupUsers.isEmpty() ? 0.0 : (double) totalScore / groupUsers.size();
            vo.setAverageScore(avg);
            vo.setMaxScore(maxScore == Integer.MIN_VALUE ? 0 : maxScore);
            vo.setMinScore(minScore == Integer.MAX_VALUE ? 0 : minScore);
            // 简化处理：问题数、已整改数暂时无法精确统计，设为0或取近似
            vo.setProblemCount(0);
            vo.setResolvedCount(0);
            vo.setCompletionRate(0.0);

            result.add(vo);
        }

        // 按平均分降序排序，并设置排名
        result.sort((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()));
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }

        return result;
    }

    // ==================== 3. 问题类型Top5 ====================

    @Override
    public List<ProblemTypeVO> getProblemTypeTop5(Integer tenantId) {
        if (tenantId == null) {
            return new ArrayList<>();
        }

        // 查询所有扣分规则（points < 0）
        LambdaQueryWrapper<PointsRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(PointsRule::getTenantId, tenantId)
                .lt(PointsRule::getPoints, 0)
                .eq(PointsRule::getStatus, 1);
        List<PointsRule> penaltyRules = pointsRuleMapper.selectList(ruleWrapper);

        if (penaltyRules.isEmpty()) {
            return new ArrayList<>();
        }

        // 统计每条规则被使用的次数（从 points_apply 中统计）
        List<ProblemTypeVO> voList = new ArrayList<>();
        for (PointsRule rule : penaltyRules) {
            LambdaQueryWrapper<PointsApply> applyWrapper = new LambdaQueryWrapper<>();
            applyWrapper.eq(PointsApply::getTenantId, tenantId)
                    .eq(PointsApply::getRuleId, rule.getId())
                    .eq(PointsApply::getSourceType, "admin");  // 只统计管理员评分

            Long count = pointsApplyMapper.selectCount(applyWrapper);
            if (count > 0) {
                // 查询涉及的用户数
                LambdaQueryWrapper<PointsApply> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(PointsApply::getTenantId, tenantId)
                        .eq(PointsApply::getRuleId, rule.getId())
                        .eq(PointsApply::getSourceType, "admin")
                        .select(PointsApply::getUserId);
                List<PointsApply> applies = pointsApplyMapper.selectList(userWrapper);
                Set<Integer> userIds = applies.stream().map(PointsApply::getUserId).collect(Collectors.toSet());

                ProblemTypeVO vo = new ProblemTypeVO();
                vo.setRuleName(rule.getRuleName());
                vo.setCategory(rule.getCategory());
                vo.setCount(count.intValue());
                vo.setUserCount(userIds.size());
                vo.setTotalPoints(Math.abs(rule.getPoints()) * count.intValue());
                vo.setAveragePoints((double) vo.getTotalPoints() / count);

                voList.add(vo);
            }
        }

        // 按扣分次数降序排序，取Top5
        voList.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        List<ProblemTypeVO> top5 = voList.stream().limit(5).collect(Collectors.toList());

        // 计算总扣分次数
        int totalCount = voList.stream().mapToInt(ProblemTypeVO::getCount).sum();
        for (ProblemTypeVO vo : top5) {
            if (totalCount > 0) {
                vo.setPercentage((double) vo.getCount() / totalCount * 100);
            } else {
                vo.setPercentage(0.0);
            }
            // 设置排名
            int idx = top5.indexOf(vo);
            vo.setRank(idx + 1);
        }

        return top5;
    }

    // ==================== 4. 效果数据 ====================

    @Override
    public EffectivenessVO getEffectivenessData(Integer tenantId) {
        if (tenantId == null) {
            return new EffectivenessVO();
        }

        EffectivenessVO vo = new EffectivenessVO();

        // 1. 整改效率指标
        LambdaQueryWrapper<RectificationTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(RectificationTask::getTenantId, tenantId);
        Long totalTasks = rectificationTaskMapper.selectCount(taskWrapper);
        long resolvedTasks = 0;
        long pendingTasks = 0;
        long overdueTasks = 0;
        double totalDays = 0;
        int resolvedCount = 0;

        if (totalTasks > 0) {
            List<RectificationTask> tasks = rectificationTaskMapper.selectList(taskWrapper);
            for (RectificationTask task : tasks) {
                if (RectificationTask.STATUS_RESOLVED.equals(task.getStatus())) {
                    resolvedTasks++;
                    if (task.getCreateTime() != null && task.getUpdateTime() != null) {
                        long days = java.time.Duration.between(task.getCreateTime(), task.getUpdateTime()).toDays();
                        totalDays += days;
                        resolvedCount++;
                    }
                } else if (RectificationTask.STATUS_PENDING.equals(task.getStatus()) ||
                        RectificationTask.STATUS_REVIEWING.equals(task.getStatus())) {
                    pendingTasks++;
                } else if (RectificationTask.STATUS_OVERDUE.equals(task.getStatus())) {
                    overdueTasks++;
                }
            }
        }

        vo.setTotalRectificationTasks(totalTasks.intValue());
        vo.setResolvedRectificationTasks((int) resolvedTasks);
        vo.setPendingRectificationTasks((int) pendingTasks);
        vo.setOverdueRectificationTasks((int) overdueTasks);
        vo.setRectificationCompletionRate(totalTasks == 0 ? 0.0 : (double) resolvedTasks / totalTasks * 100);
        vo.setAverageRectificationDays(resolvedCount == 0 ? 0.0 : totalDays / resolvedCount);

        // 2. 参与度指标
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getTenantId, tenantId)
                .eq(User::getRole, "VILLAGER");
        Long totalHouseholds = userMapper.selectCount(userWrapper);

        // 有积分记录的用户（points_apply 中存在的用户）
        LambdaQueryWrapper<PointsApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(PointsApply::getTenantId, tenantId)
                .select(PointsApply::getUserId);
        List<PointsApply> applies = pointsApplyMapper.selectList(applyWrapper);
        Set<Integer> participantIds = applies.stream().map(PointsApply::getUserId).collect(Collectors.toSet());
        int participantCount = participantIds.size();

        vo.setTotalHouseholds(totalHouseholds.intValue());
        vo.setParticipantCount(participantCount);
        vo.setParticipationRate(totalHouseholds == 0 ? 0.0 : (double) participantCount / totalHouseholds * 100);

        // 参与率变化（与上月对比）-- 简化，暂设为0
        vo.setParticipationRateChange(0.0);
        vo.setNewParticipants(0);
        vo.setActiveUsers(participantCount);  // 近似

        // 3. 红黑榜指标（取最新快照与上个月对比）
        try {
            // 获取最新快照（返回 SnapshotVO）
            SnapshotVO latestSnapshot = publishSnapshotService.getLatestSnapshot(tenantId);
            if (latestSnapshot != null) {
                vo.setCurrentRedCount(latestSnapshot.getRedCount() != null ? latestSnapshot.getRedCount() : 0);
                vo.setCurrentBlackCount(latestSnapshot.getBlackCount() != null ? latestSnapshot.getBlackCount() : 0);
                vo.setCurrentRedList(latestSnapshot.getRedList());
                vo.setCurrentBlackList(latestSnapshot.getBlackList());

                // 获取上个月快照
                String month = latestSnapshot.getMonth();
                String prevMonth = getPreviousMonth(month);
                if (prevMonth != null) {
                    List<SnapshotVO> prevSnapshots = publishSnapshotService.getSnapshotsByMonth(prevMonth, tenantId);
                    if (!prevSnapshots.isEmpty()) {
                        SnapshotVO prev = prevSnapshots.get(0);
                        vo.setPreviousRedCount(prev.getRedCount() != null ? prev.getRedCount() : 0);
                        vo.setPreviousBlackCount(prev.getBlackCount() != null ? prev.getBlackCount() : 0);
                        vo.setPreviousRedList(prev.getRedList());
                        vo.setPreviousBlackList(prev.getBlackList());
                    } else {
                        vo.setPreviousRedCount(0);
                        vo.setPreviousBlackCount(0);
                        vo.setPreviousRedList(new ArrayList<>());
                        vo.setPreviousBlackList(new ArrayList<>());
                    }
                } else {
                    vo.setPreviousRedCount(0);
                    vo.setPreviousBlackCount(0);
                    vo.setPreviousRedList(new ArrayList<>());
                    vo.setPreviousBlackList(new ArrayList<>());
                }
            } else {
                vo.setCurrentRedCount(0);
                vo.setCurrentBlackCount(0);
                vo.setPreviousRedCount(0);
                vo.setPreviousBlackCount(0);
                vo.setCurrentRedList(new ArrayList<>());
                vo.setCurrentBlackList(new ArrayList<>());
                vo.setPreviousRedList(new ArrayList<>());
                vo.setPreviousBlackList(new ArrayList<>());
            }
        } catch (Exception e) {
            log.warn("获取红黑榜数据失败: {}", e.getMessage());
            vo.setCurrentRedCount(0);
            vo.setCurrentBlackCount(0);
            vo.setPreviousRedCount(0);
            vo.setPreviousBlackCount(0);
            vo.setCurrentRedList(new ArrayList<>());
            vo.setCurrentBlackList(new ArrayList<>());
            vo.setPreviousRedList(new ArrayList<>());
            vo.setPreviousBlackList(new ArrayList<>());
        }

        // 计算增长率
        if (vo.getPreviousRedCount() > 0) {
            vo.setRedListGrowthRate((double) (vo.getCurrentRedCount() - vo.getPreviousRedCount()) / vo.getPreviousRedCount() * 100);
        } else {
            vo.setRedListGrowthRate(vo.getCurrentRedCount() > 0 ? 100.0 : 0.0);
        }
        if (vo.getPreviousBlackCount() > 0) {
            vo.setBlackListReductionRate((double) (vo.getPreviousBlackCount() - vo.getCurrentBlackCount()) / vo.getPreviousBlackCount() * 100);
        } else {
            vo.setBlackListReductionRate(vo.getCurrentBlackCount() == 0 ? 100.0 : 0.0);
        }

        // 4. 综合评分和等级（简化）
        double score = 0;
        if (vo.getRectificationCompletionRate() != null) score += vo.getRectificationCompletionRate() * 0.4;
        if (vo.getParticipationRate() != null) score += vo.getParticipationRate() * 0.3;
        if (vo.getRedListGrowthRate() != null && vo.getRedListGrowthRate() > 0) score += Math.min(vo.getRedListGrowthRate(), 100) * 0.15;
        if (vo.getBlackListReductionRate() != null && vo.getBlackListReductionRate() > 0) score += Math.min(vo.getBlackListReductionRate(), 100) * 0.15;
        vo.setOverallScore((int) Math.min(score, 100));

        if (vo.getOverallScore() >= 80) vo.setGrade("优秀");
        else if (vo.getOverallScore() >= 60) vo.setGrade("良好");
        else if (vo.getOverallScore() >= 40) vo.setGrade("一般");
        else vo.setGrade("待提升");

        // 总结语
        if (vo.getOverallScore() >= 80) {
            vo.setSummary("治理效果显著，继续保持！");
        } else if (vo.getOverallScore() >= 60) {
            vo.setSummary("治理效果良好，部分指标有提升空间。");
        } else if (vo.getOverallScore() >= 40) {
            vo.setSummary("治理效果一般，建议重点关注黑榜问题。");
        } else {
            vo.setSummary("治理效果待提升，建议加大整改力度。");
        }

        return vo;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取最近6个月的月份列表（从当前月往前推6个月）
     */
    private List<String> getLastSixMonths() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            months.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return months;
    }

    /**
     * 获取上个月的月份字符串（yyyy-MM）
     */
    private String getPreviousMonth(String month) {
        try {
            LocalDate date = LocalDate.parse(month + "-01");
            LocalDate prev = date.minusMonths(1);
            return prev.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (Exception e) {
            return null;
        }
    }
}