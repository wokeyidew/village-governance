package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.points.entity.QuarterlySnapshot;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.mapper.QuarterlySnapshotMapper;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.ActivityParticipationMapper;
import com.scau.village.module.points.service.QuarterlySnapshotService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 季度快照 Service 实现类
 * 对应表：quarterly_snapshot
 * 提供季度结算、榜单查询、个人数据查询等功能
 *
 * @author system
 * @since 2026-08-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuarterlySnapshotServiceImpl extends ServiceImpl<QuarterlySnapshotMapper, QuarterlySnapshot>
        implements QuarterlySnapshotService {

    private final QuarterlySnapshotMapper snapshotMapper;
    private final UserMapper userMapper;
    private final PointsFlowMapper pointsFlowMapper;
    private final PointsApplyMapper pointsApplyMapper;
    private final ActivityParticipationMapper activityParticipationMapper;

    // ==================== 标签常量 ====================

    private static final String TAG_RED = "red";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_WARNING = "warning";
    private static final String TAG_NORMAL = "normal";

    // ==================== 季度结算 ====================

    @Override
    @Transactional
    public void settleQuarter(String quarter, Integer tenantId) {
        log.info("【季度结算】开始结算季度: {}, tenantId: {}", quarter, tenantId);

        // 1. 获取该租户下所有村民用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getTenantId, tenantId)
                   .eq(User::getRole, "VILLAGER")
                   .eq(User::getDeleted, 0);
        List<User> users = userMapper.selectList(userWrapper);
        if (users.isEmpty()) {
            log.warn("【季度结算】租户 {} 下没有村民用户，跳过结算", tenantId);
            return;
        }
        log.info("【季度结算】共有 {} 户村民需要结算", users.size());

        // 2. 计算季度时间范围
        LocalDateTime quarterStart = getQuarterStartTime(quarter);
        LocalDateTime quarterEnd = getQuarterEndTime(quarter);
        log.info("【季度结算】季度时间范围: {} ~ {}", quarterStart, quarterEnd);

        // 3. 遍历用户，计算季度数据
        List<QuarterlySnapshot> snapshots = new ArrayList<>();
        for (User user : users) {
            Integer userId = user.getId();

            // 3.1 计算本季获得积分（只统计加分）
            Integer earnedPoints = calculateEarnedPoints(userId, quarterStart, quarterEnd);

            // 3.2 计算本季净积分（加分 - 扣分 + 整改恢复）
            Integer netPoints = calculateNetPoints(userId, quarterStart, quarterEnd);

            // 3.3 计算加分事项数量（去重，按 points_apply 记录数统计）
            Integer ruleCount = calculateRuleCount(userId, quarterStart, quarterEnd);

            // 3.4 计算活动参与次数
            Integer activityCount = calculateActivityCount(userId, quarterStart, quarterEnd);

            // 3.5 计算连续零扣分天数
            Integer noPenaltyDays = calculateNoPenaltyDays(userId, quarterStart, quarterEnd);

            // 3.6 获取本季最后一次积分变动时间
            LocalDateTime lastActivityTime = getLastActivityTime(userId, quarterStart, quarterEnd);

            // 3.7 构建快照对象
            QuarterlySnapshot snapshot = new QuarterlySnapshot();
            snapshot.setTenantId(tenantId);
            snapshot.setQuarter(quarter);
            snapshot.setUserId(userId);
            snapshot.setQuarterEarnedPoints(earnedPoints);
            snapshot.setQuarterNetPoints(netPoints);
            snapshot.setRuleCount(ruleCount);
            snapshot.setActivityCount(activityCount);
            snapshot.setNoPenaltyDays(noPenaltyDays);
            snapshot.setLastActivityTime(lastActivityTime);
            snapshot.setCreateTime(LocalDateTime.now());
            snapshot.setUpdateTime(LocalDateTime.now());

            // 检查是否已存在（使用唯一键 uk_user_quarter）
            QuarterlySnapshot existing = snapshotMapper.selectUserQuarterData(tenantId, quarter, userId);
            if (existing != null) {
                snapshot.setId(existing.getId());
                snapshotMapper.updateById(snapshot);
                log.debug("【季度结算】更新用户 {} 的快照数据，id={}", userId, snapshot.getId());
            } else {
                snapshotMapper.insert(snapshot);
                log.debug("【季度结算】新增用户 {} 的快照数据，id={}", userId, snapshot.getId());
            }

            snapshots.add(snapshot);
        }

        // 4. 计算上季度数据，得出进步分
        String prevQuarter = getPreviousQuarter(quarter);
        for (QuarterlySnapshot snapshot : snapshots) {
            QuarterlySnapshot prevSnapshot = snapshotMapper.selectUserQuarterData(tenantId, prevQuarter, snapshot.getUserId());
            if (prevSnapshot != null) {
                snapshot.setPreviousQuarterPoints(prevSnapshot.getQuarterEarnedPoints());
                snapshot.setProgressPoints(snapshot.getQuarterEarnedPoints() - prevSnapshot.getQuarterEarnedPoints());
            } else {
                snapshot.setPreviousQuarterPoints(0);
                snapshot.setProgressPoints(0);
            }
            // 更新进步分
            snapshotMapper.updateById(snapshot);
        }

        // 5. 计算积分排名（按4个维度降序）
        List<QuarterlySnapshot> sortedByPoints = snapshots.stream()
                .sorted(Comparator
                        .comparing(QuarterlySnapshot::getQuarterEarnedPoints, Comparator.reverseOrder())
                        .thenComparing(QuarterlySnapshot::getRuleCount, Comparator.reverseOrder())
                        .thenComparing(QuarterlySnapshot::getActivityCount, Comparator.reverseOrder())
                        .thenComparing(QuarterlySnapshot::getNoPenaltyDays, Comparator.reverseOrder())
                        .thenComparing(QuarterlySnapshot::getLastActivityTime, Comparator.naturalOrder())
                )
                .collect(Collectors.toList());

        int rank = 1;
        for (int i = 0; i < sortedByPoints.size(); i++) {
            QuarterlySnapshot current = sortedByPoints.get(i);
            if (i > 0) {
                QuarterlySnapshot previous = sortedByPoints.get(i - 1);
                if (!isSameRank(current, previous)) {
                    rank = i + 1;
                }
            }
            current.setRankPoints(rank);
            snapshotMapper.updateById(current);
        }

        // 6. 计算进步排名（按进步分降序）
        List<QuarterlySnapshot> sortedByProgress = snapshots.stream()
                .filter(s -> s.getProgressPoints() > 0)
                .sorted(Comparator.comparing(QuarterlySnapshot::getProgressPoints, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int progressRank = 1;
        for (int i = 0; i < sortedByProgress.size(); i++) {
            QuarterlySnapshot current = sortedByProgress.get(i);
            if (i > 0) {
                QuarterlySnapshot previous = sortedByProgress.get(i - 1);
                if (!Objects.equals(current.getProgressPoints(), previous.getProgressPoints())) {
                    progressRank = i + 1;
                }
            }
            current.setRankProgress(progressRank);
            snapshotMapper.updateById(current);
        }

        // 7. 标记标签
        int totalCount = snapshots.size();
        int redCount = Math.max(3, (int) Math.ceil(totalCount * 0.1));
        int progressCount = Math.max(3, (int) Math.ceil(totalCount * 0.1));

        // 获取已排名的列表用于打标
        List<QuarterlySnapshot> rankedList = sortedByPoints;

        for (int i = 0; i < rankedList.size(); i++) {
            QuarterlySnapshot snapshot = rankedList.get(i);
            String tag;

            // 7.1 红榜：排名在前 10%
            if (i < redCount) {
                tag = TAG_RED;
            }
            // 7.2 蜕变榜：有进步且进步排名在前 10%
            else if (snapshot.getProgressPoints() > 0 && snapshot.getRankProgress() != null && snapshot.getRankProgress() <= progressCount) {
                tag = TAG_PROGRESS;
            }
            // 7.3 帮扶榜：净积分为负
            else if (snapshot.getQuarterNetPoints() < 0) {
                tag = TAG_WARNING;
            }
            // 7.4 普通
            else {
                tag = TAG_NORMAL;
            }

            snapshot.setTag(tag);
            snapshotMapper.updateById(snapshot);
        }

        log.info("【季度结算】结算完成，共处理 {} 户，红榜 {} 户，蜕变榜 {} 户，帮扶榜 {} 户",
                totalCount, redCount, progressCount,
                snapshots.stream().filter(s -> TAG_WARNING.equals(s.getTag())).count());
    }

    @Override
    @Async
    public void settleQuarterAsync(String quarter, Integer tenantId) {
        settleQuarter(quarter, tenantId);
    }

    // ==================== 榜单查询 ====================

    @Override
    public List<QuarterlySnapshot> getRedList(Integer tenantId, String quarter) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.selectRedList(tenantId, quarter, 20);
    }

    @Override
    public List<QuarterlySnapshot> getRedList(Integer tenantId, String quarter, Integer pageNum, Integer pageSize) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        // 简单分页，实际项目可用 MyBatis-Plus Page
        int offset = (pageNum - 1) * pageSize;
        return snapshotMapper.selectRedList(tenantId, quarter, offset + pageSize)
                .stream().skip(offset).limit(pageSize).collect(Collectors.toList());
    }

    @Override
    public List<QuarterlySnapshot> getProgressList(Integer tenantId, String quarter) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.selectProgressList(tenantId, quarter, 20);
    }

    @Override
    public List<QuarterlySnapshot> getProgressList(Integer tenantId, String quarter, Integer pageNum, Integer pageSize) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        int offset = (pageNum - 1) * pageSize;
        return snapshotMapper.selectProgressList(tenantId, quarter, offset + pageSize)
                .stream().skip(offset).limit(pageSize).collect(Collectors.toList());
    }

    @Override
    public List<QuarterlySnapshot> getWarningList(Integer tenantId, String quarter) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.selectWarningList(tenantId, quarter);
    }

    // ==================== 个人数据查询 ====================

    @Override
    public QuarterlySnapshot getUserQuarterData(Integer tenantId, String quarter, Integer userId) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.selectUserQuarterData(tenantId, quarter, userId);
    }

    @Override
    public QuarterlySnapshot getUserQuarterDataOrDefault(Integer tenantId, String quarter, Integer userId) {
        QuarterlySnapshot data = getUserQuarterData(tenantId, quarter, userId);
        if (data == null) {
            data = new QuarterlySnapshot();
            data.setTenantId(tenantId);
            data.setQuarter(quarter);
            data.setUserId(userId);
            data.setQuarterEarnedPoints(0);
            data.setQuarterNetPoints(0);
            data.setPreviousQuarterPoints(0);
            data.setProgressPoints(0);
            data.setRankPoints(null);
            data.setRankProgress(null);
            data.setTag(TAG_NORMAL);
            data.setRuleCount(0);
            data.setActivityCount(0);
            data.setNoPenaltyDays(0);
            data.setLastActivityTime(null);
        }
        return data;
    }

    @Override
    public List<QuarterlySnapshot> getUserHistory(Integer tenantId, Integer userId) {
        LambdaQueryWrapper<QuarterlySnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuarterlySnapshot::getTenantId, tenantId)
               .eq(QuarterlySnapshot::getUserId, userId)
               .orderByDesc(QuarterlySnapshot::getQuarter);
        return snapshotMapper.selectList(wrapper);
    }

    @Override
    public Integer getUserRank(Integer tenantId, String quarter, Integer userId) {
        QuarterlySnapshot data = getUserQuarterData(tenantId, quarter, userId);
        return data != null ? data.getRankPoints() : null;
    }

    // ==================== 统计查询 ====================

    @Override
    public Integer countRedList(Integer tenantId, String quarter) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.countRedList(tenantId, quarter);
    }

    @Override
    public Integer countProgressList(Integer tenantId, String quarter) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.countProgressList(tenantId, quarter);
    }

    @Override
    public Integer countWarningList(Integer tenantId, String quarter) {
        if (quarter == null) {
            quarter = getCurrentQuarter();
        }
        return snapshotMapper.countWarningList(tenantId, quarter);
    }

    // ==================== 工具方法 ====================

    @Override
    public String getCurrentQuarter() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int q = (month - 1) / 3 + 1;
        return year + "-Q" + q;
    }

    @Override
    public String getQuarterByDate(Integer year, Integer month) {
        int q = (month - 1) / 3 + 1;
        return year + "-Q" + q;
    }

    @Override
    public String getPreviousQuarter(String quarter) {
        String[] parts = quarter.split("-Q");
        int year = Integer.parseInt(parts[0]);
        int q = Integer.parseInt(parts[1]);
        if (q == 1) {
            return (year - 1) + "-Q4";
        } else {
            return year + "-Q" + (q - 1);
        }
    }

    @Override
    public LocalDateTime getQuarterStartTime(String quarter) {
        String[] parts = quarter.split("-Q");
        int year = Integer.parseInt(parts[0]);
        int q = Integer.parseInt(parts[1]);
        int month = (q - 1) * 3 + 1;
        return LocalDate.of(year, month, 1).atStartOfDay();
    }

    @Override
    public LocalDateTime getQuarterEndTime(String quarter) {
        String[] parts = quarter.split("-Q");
        int year = Integer.parseInt(parts[0]);
        int q = Integer.parseInt(parts[1]);
        int month = q * 3;
        LocalDate lastDay = YearMonth.of(year, month).atEndOfMonth();
        return lastDay.atTime(LocalTime.MAX);
    }

    @Override
    public List<String> getAvailableQuarters(Integer tenantId) {
        LambdaQueryWrapper<QuarterlySnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuarterlySnapshot::getTenantId, tenantId)
               .select(QuarterlySnapshot::getQuarter)
               .groupBy(QuarterlySnapshot::getQuarter)
               .orderByDesc(QuarterlySnapshot::getQuarter);
        List<QuarterlySnapshot> list = snapshotMapper.selectList(wrapper);
        return list.stream().map(QuarterlySnapshot::getQuarter).collect(Collectors.toList());
    }

    // ==================== 手动触发 ====================

    @Override
    @Transactional
    public void settleQuarterManual(String quarter, Integer tenantId, Boolean force) {
        if (force != null && force) {
            // 强制重新计算：删除已有数据
            deleteQuarterData(quarter, tenantId);
        }
        settleQuarter(quarter, tenantId);
    }

    @Override
    @Transactional
    public Integer deleteQuarterData(String quarter, Integer tenantId) {
        LambdaQueryWrapper<QuarterlySnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuarterlySnapshot::getTenantId, tenantId)
               .eq(QuarterlySnapshot::getQuarter, quarter);
        return snapshotMapper.delete(wrapper);
    }

    // ==================== 同名次排序 ====================

    @Override
    public boolean isSameRank(QuarterlySnapshot a, QuarterlySnapshot b) {
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.getQuarterEarnedPoints(), b.getQuarterEarnedPoints())
                && Objects.equals(a.getRuleCount(), b.getRuleCount())
                && Objects.equals(a.getActivityCount(), b.getActivityCount())
                && Objects.equals(a.getNoPenaltyDays(), b.getNoPenaltyDays())
                && Objects.equals(a.getLastActivityTime(), b.getLastActivityTime());
    }

    // ==================== 私有辅助计算方法 ====================

    /**
     * 计算本季获得积分（只统计加分项）
     */
    private Integer calculateEarnedPoints(Integer userId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PointsFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsFlow::getUserId, userId)
               .gt(PointsFlow::getChangeAmount, 0)
               .ge(PointsFlow::getCreateTime, start)
               .le(PointsFlow::getCreateTime, end);
        List<PointsFlow> flows = pointsFlowMapper.selectList(wrapper);
        return flows.stream().mapToInt(PointsFlow::getChangeAmount).sum();
    }

    /**
     * 计算本季净积分（加分 - 扣分 + 整改恢复）
     */
    private Integer calculateNetPoints(Integer userId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PointsFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsFlow::getUserId, userId)
               .ge(PointsFlow::getCreateTime, start)
               .le(PointsFlow::getCreateTime, end);
        List<PointsFlow> flows = pointsFlowMapper.selectList(wrapper);
        return flows.stream().mapToInt(PointsFlow::getChangeAmount).sum();
    }

    /**
     * 计算加分事项数量（按 points_apply 记录数统计）
     * 说明：村民每次申报或管理员每次评分都计为1个事项
     */
    private Integer calculateRuleCount(Integer userId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PointsApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsApply::getUserId, userId)
               .eq(PointsApply::getStatus, "approved")
               .ge(PointsApply::getCreateTime, start)
               .le(PointsApply::getCreateTime, end);
        return pointsApplyMapper.selectCount(wrapper).intValue();
    }

    /**
     * 计算活动参与次数
     */
    private Integer calculateActivityCount(Integer userId, LocalDateTime start, LocalDateTime end) {
        return activityParticipationMapper.countByUserIdAndDateRange(userId, start, end);
    }

    /**
     * 计算连续零扣分天数
     * 逻辑：查找本季度最后一条扣分记录，计算距季度结束的天数
     * 如果没有扣分记录，返回本季度总天数
     */
    private Integer calculateNoPenaltyDays(Integer userId, LocalDateTime start, LocalDateTime end) {
        // 查询本季度所有扣分记录（change_amount < 0）
        LambdaQueryWrapper<PointsFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsFlow::getUserId, userId)
               .lt(PointsFlow::getChangeAmount, 0)
               .ge(PointsFlow::getCreateTime, start)
               .le(PointsFlow::getCreateTime, end)
               .orderByDesc(PointsFlow::getCreateTime)
               .last("LIMIT 1");
        PointsFlow lastPenalty = pointsFlowMapper.selectOne(wrapper);

        if (lastPenalty == null) {
            // 没有扣分记录，返回本季度总天数
            return (int) java.time.Duration.between(start, end).toDays() + 1;
        }

        // 计算从最后扣分日期到季度结束的天数
        LocalDate penaltyDate = lastPenalty.getCreateTime().toLocalDate();
        LocalDate endDate = end.toLocalDate();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(penaltyDate, endDate);
    }

    /**
     * 获取本季最后一次积分变动时间
     */
    private LocalDateTime getLastActivityTime(Integer userId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<PointsFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsFlow::getUserId, userId)
               .ge(PointsFlow::getCreateTime, start)
               .le(PointsFlow::getCreateTime, end)
               .orderByDesc(PointsFlow::getCreateTime)
               .last("LIMIT 1");
        PointsFlow lastFlow = pointsFlowMapper.selectOne(wrapper);
        return lastFlow != null ? lastFlow.getCreateTime() : null;
    }

}