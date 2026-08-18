package com.scau.village.module.points.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.InspectionHousehold;
import com.scau.village.module.points.entity.PublishSnapshot;
import com.scau.village.module.points.mapper.InspectionHouseholdMapper;
import com.scau.village.module.points.mapper.PublishSnapshotMapper;
import com.scau.village.module.points.service.PublishSnapshotService;
import com.scau.village.module.points.vo.CompareVO;
import com.scau.village.module.points.vo.SnapshotVO;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 红黑榜公示快照服务实现类
 *
 * @author system
 * @since 2026-08-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublishSnapshotServiceImpl extends ServiceImpl<PublishSnapshotMapper, PublishSnapshot>
        implements PublishSnapshotService {

    private final PublishSnapshotMapper publishSnapshotMapper;
    private final InspectionHouseholdMapper inspectionHouseholdMapper;
    private final UserMapper userMapper;

    private static final double RED_RATIO = 0.1;   // 红榜比例前10%
    private static final double BLACK_RATIO = 0.1; // 黑榜比例后10%

    // ==================== 生成与发布方法 ====================

    @Override
    public SnapshotVO generateSnapshot(Long batchId, Integer tenantId) {
        if (batchId == null || tenantId == null) {
            throw new BusinessException("批次ID和租户ID不能为空");
        }

        // 1. 获取该批次下的所有户汇总记录
        LambdaQueryWrapper<InspectionHousehold> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InspectionHousehold::getBatchId, batchId)
                .eq(InspectionHousehold::getTenantId, tenantId.longValue())
                .eq(InspectionHousehold::getDeleted, 0);
        List<InspectionHousehold> households = inspectionHouseholdMapper.selectList(wrapper);

        if (households.isEmpty()) {
            throw new BusinessException("该批次暂无评分数据，无法生成榜单");
        }

        // 2. 按总分降序排序
        households.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));

        // 3. 构建快照条目
        List<PublishSnapshot.SnapshotItem> items = new ArrayList<>();
        int total = households.size();
        int redCount = Math.max(1, (int) Math.round(total * RED_RATIO)); // 至少1户
        int blackCount = Math.max(1, (int) Math.round(total * BLACK_RATIO));

        // 红榜索引：0 到 redCount-1
        // 黑榜索引：total - blackCount 到 total-1
        int blackStart = total - blackCount;

        for (int i = 0; i < total; i++) {
            InspectionHousehold h = households.get(i);
            User user = userMapper.selectById(h.getUserId());
            String userName = user != null ? user.getRealName() : "未知";

            PublishSnapshot.SnapshotItem item = new PublishSnapshot.SnapshotItem();
            item.setUserId(h.getUserId());
            item.setUserName(userName);
            item.setTotalScore(h.getTotalScore());
            item.setRank(i + 1);

            // 标签判断
            if (i < redCount) {
                item.setTag("red");
            } else if (i >= blackStart) {
                item.setTag("black");
            } else {
                item.setTag("normal");
            }
            items.add(item);
        }

        // 4. 构建红榜和黑榜ID列表
        List<Long> redIds = items.stream()
                .filter(item -> "red".equals(item.getTag()))
                .map(PublishSnapshot.SnapshotItem::getUserId)
                .collect(Collectors.toList());
        List<Long> blackIds = items.stream()
                .filter(item -> "black".equals(item.getTag()))
                .map(PublishSnapshot.SnapshotItem::getUserId)
                .collect(Collectors.toList());

        // 5. 封装VO
        SnapshotVO vo = new SnapshotVO();
        vo.setBatchId(batchId);
        vo.setTotalItems(total);
        vo.setRedCount(redIds.size());
        vo.setBlackCount(blackIds.size());
        vo.setItems(items);
        vo.setRedList(redIds);
        vo.setBlackList(blackIds);

        log.info("生成快照数据成功，批次ID={}, 总户数={}, 红榜{}户, 黑榜{}户",
                batchId, total, redIds.size(), blackIds.size());
        return vo;
    }

    @Override
    @Transactional
    public PublishSnapshot publishSnapshot(Long batchId, Integer tenantId, Long publisherId, String publisherName) {
        // 1. 检查是否已发布
        if (isPublished(batchId)) {
            throw new BusinessException("该批次已发布榜单，请勿重复发布");
        }

        // 2. 生成快照数据
        SnapshotVO snapshotVO = generateSnapshot(batchId, tenantId);

        // 3. 提取月份（从批次日期或当前时间）
        String month = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 4. 保存快照
        PublishSnapshot snapshot = new PublishSnapshot();
        snapshot.setBatchId(batchId);
        snapshot.setMonth(month);
        snapshot.setSnapshotData(JSON.toJSONString(snapshotVO.getItems()));
        snapshot.setRedList(JSON.toJSONString(snapshotVO.getRedList()));
        snapshot.setBlackList(JSON.toJSONString(snapshotVO.getBlackList()));
        snapshot.setPublishTime(LocalDateTime.now());
        snapshot.setPublishBy(publisherId);
        snapshot.setPublishByName(publisherName);
        snapshot.setTenantId(tenantId);
        snapshot.setCreateTime(LocalDateTime.now());
        snapshot.setUpdateTime(LocalDateTime.now());
        snapshot.setDeleted(0);

        save(snapshot);
        log.info("发布红黑榜快照成功，批次ID={}, 快照ID={}", batchId, snapshot.getId());
        return snapshot;
    }

    // ==================== 查询方法 ====================

    @Override
    public PublishSnapshot getByBatchId(Long batchId) {
        if (batchId == null) {
            return null;
        }
        return publishSnapshotMapper.selectByBatchId(batchId);
    }

    @Override
    public SnapshotVO getSnapshotByBatchId(Long batchId) {
        PublishSnapshot snapshot = getByBatchId(batchId);
        if (snapshot == null) {
            return null;
        }
        return convertToVO(snapshot);
    }

    @Override
    public List<SnapshotVO> getSnapshotsByMonth(String month, Integer tenantId) {
        if (StringUtils.isBlank(month) || tenantId == null) {
            return Collections.emptyList();
        }
        List<PublishSnapshot> list = publishSnapshotMapper.selectByTenantAndMonth(tenantId, month);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public SnapshotVO getLatestSnapshot(Integer tenantId) {
        if (tenantId == null) {
            return null;
        }
        PublishSnapshot snapshot = publishSnapshotMapper.selectLatest(tenantId);
        return snapshot != null ? convertToVO(snapshot) : null;
    }

    @Override
    public List<SnapshotVO> getSnapshotsBeforeMonth(Integer tenantId, String month) {
        if (tenantId == null || StringUtils.isBlank(month)) {
            return Collections.emptyList();
        }
        List<PublishSnapshot> list = publishSnapshotMapper.selectBeforeMonth(tenantId, month);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    // ==================== 月度对比方法 ====================

    @Override
    public CompareVO compareMonths(String currentMonth, String previousMonth, Integer tenantId) {
        if (StringUtils.isBlank(currentMonth) || StringUtils.isBlank(previousMonth) || tenantId == null) {
            throw new BusinessException("参数不完整");
        }

        // 1. 获取两个月份的快照（取最新的一份）
        List<PublishSnapshot> currentList = publishSnapshotMapper.selectByTenantAndMonth(tenantId, currentMonth);
        List<PublishSnapshot> previousList = publishSnapshotMapper.selectByTenantAndMonth(tenantId, previousMonth);

        if (currentList.isEmpty()) {
            throw new BusinessException("当前月份无快照数据");
        }
        if (previousList.isEmpty()) {
            throw new BusinessException("对比月份无快照数据");
        }

        // 取最新的快照
        PublishSnapshot current = currentList.get(0);
        PublishSnapshot previous = previousList.get(0);

        // 2. 解析红黑榜ID列表
        List<Long> currentRed = JSON.parseArray(current.getRedList(), Long.class);
        List<Long> currentBlack = JSON.parseArray(current.getBlackList(), Long.class);
        List<Long> previousRed = JSON.parseArray(previous.getRedList(), Long.class);
        List<Long> previousBlack = JSON.parseArray(previous.getBlackList(), Long.class);

        // 3. 计算变化
        // 红榜新增：当前红榜 - 上月红榜
        List<Long> redAdded = new ArrayList<>(currentRed);
        redAdded.removeAll(previousRed);
        // 红榜退出：上月红榜 - 当前红榜
        List<Long> redExited = new ArrayList<>(previousRed);
        redExited.removeAll(currentRed);

        // 黑榜新增：当前黑榜 - 上月黑榜
        List<Long> blackAdded = new ArrayList<>(currentBlack);
        blackAdded.removeAll(previousBlack);
        // 黑榜退出：上月黑榜 - 当前黑榜
        List<Long> blackExited = new ArrayList<>(previousBlack);
        blackExited.removeAll(currentBlack);

        // 4. 统计用户排名变化
        List<CompareVO.RankChange> rankChanges = new ArrayList<>();
        List<PublishSnapshot.SnapshotItem> currentItems = JSON.parseArray(current.getSnapshotData(), PublishSnapshot.SnapshotItem.class);
        List<PublishSnapshot.SnapshotItem> previousItems = JSON.parseArray(previous.getSnapshotData(), PublishSnapshot.SnapshotItem.class);

        Map<Long, Integer> previousRankMap = previousItems.stream()
                .collect(Collectors.toMap(PublishSnapshot.SnapshotItem::getUserId, PublishSnapshot.SnapshotItem::getRank));
        Map<Long, Integer> currentRankMap = currentItems.stream()
                .collect(Collectors.toMap(PublishSnapshot.SnapshotItem::getUserId, PublishSnapshot.SnapshotItem::getRank));

        Set<Long> allUsers = new HashSet<>(previousRankMap.keySet());
        allUsers.addAll(currentRankMap.keySet());

        for (Long userId : allUsers) {
            Integer prevRank = previousRankMap.get(userId);
            Integer currRank = currentRankMap.get(userId);
            if (prevRank != null && currRank != null) {
                CompareVO.RankChange change = new CompareVO.RankChange();
                change.setUserId(userId);
                String userName = previousItems.stream()
                        .filter(item -> item.getUserId().equals(userId))
                        .findFirst()
                        .map(PublishSnapshot.SnapshotItem::getUserName)
                        .orElse("未知");
                change.setUserName(userName);
                change.setPreviousRank(prevRank);
                change.setCurrentRank(currRank);
                change.setChange(currRank - prevRank);
                rankChanges.add(change);
            }
        }

        rankChanges.sort(Comparator.comparingInt(CompareVO.RankChange::getChange));

        // 5. 封装对比VO
        CompareVO compareVO = new CompareVO();
        compareVO.setCurrentMonth(currentMonth);
        compareVO.setPreviousMonth(previousMonth);
        compareVO.setCurrentRedCount(currentRed.size());
        compareVO.setPreviousRedCount(previousRed.size());
        compareVO.setCurrentBlackCount(currentBlack.size());
        compareVO.setPreviousBlackCount(previousBlack.size());
        compareVO.setRedAdded(redAdded.size());
        compareVO.setRedExited(redExited.size());
        compareVO.setBlackAdded(blackAdded.size());
        compareVO.setBlackExited(blackExited.size());
        compareVO.setRedAddedList(redAdded);
        compareVO.setRedExitedList(redExited);
        compareVO.setBlackAddedList(blackAdded);
        compareVO.setBlackExitedList(blackExited);
        compareVO.setRankChanges(rankChanges);

        log.info("月度对比完成，当前月份={}, 对比月份={}", currentMonth, previousMonth);
        return compareVO;
    }

    @Override
    public CompareVO compareWithPreviousMonth(Integer tenantId) {
        if (tenantId == null) {
            throw new BusinessException("租户ID不能为空");
        }
        PublishSnapshot latest = publishSnapshotMapper.selectLatest(tenantId);
        if (latest == null) {
            throw new BusinessException("暂无快照数据");
        }
        String currentMonth = latest.getMonth();

        List<PublishSnapshot> previousList = publishSnapshotMapper.selectBeforeMonth(tenantId, currentMonth);
        if (previousList.isEmpty()) {
            throw new BusinessException("没有更早的月份数据，无法对比");
        }
        String previousMonth = previousList.get(0).getMonth();

        return compareMonths(currentMonth, previousMonth, tenantId);
    }

    @Override
    public CompareVO.RankChange getUserRankChange(Long userId, String startMonth, String endMonth, Integer tenantId) {
        if (userId == null || StringUtils.isBlank(startMonth) || StringUtils.isBlank(endMonth) || tenantId == null) {
            throw new BusinessException("参数不完整");
        }

        List<PublishSnapshot> startList = publishSnapshotMapper.selectByTenantAndMonth(tenantId, startMonth);
        List<PublishSnapshot> endList = publishSnapshotMapper.selectByTenantAndMonth(tenantId, endMonth);

        if (startList.isEmpty() || endList.isEmpty()) {
            throw new BusinessException("指定的月份无快照数据");
        }

        PublishSnapshot startSnapshot = startList.get(0);
        PublishSnapshot endSnapshot = endList.get(0);

        List<PublishSnapshot.SnapshotItem> startItems = JSON.parseArray(startSnapshot.getSnapshotData(), PublishSnapshot.SnapshotItem.class);
        List<PublishSnapshot.SnapshotItem> endItems = JSON.parseArray(endSnapshot.getSnapshotData(), PublishSnapshot.SnapshotItem.class);

        PublishSnapshot.SnapshotItem startItem = startItems.stream()
                .filter(item -> item.getUserId().equals(userId))
                .findFirst().orElse(null);
        PublishSnapshot.SnapshotItem endItem = endItems.stream()
                .filter(item -> item.getUserId().equals(userId))
                .findFirst().orElse(null);

        CompareVO.RankChange change = new CompareVO.RankChange();
        change.setUserId(userId);
        if (startItem != null) {
            change.setPreviousRank(startItem.getRank());
            change.setUserName(startItem.getUserName());
        } else {
            change.setPreviousRank(null);
            change.setUserName("未参与");
        }
        if (endItem != null) {
            change.setCurrentRank(endItem.getRank());
            if (change.getUserName() == null || "未参与".equals(change.getUserName())) {
                change.setUserName(endItem.getUserName());
            }
        } else {
            change.setCurrentRank(null);
        }

        if (change.getPreviousRank() != null && change.getCurrentRank() != null) {
            change.setChange(change.getCurrentRank() - change.getPreviousRank());
        } else {
            change.setChange(null);
        }

        return change;
    }

    // ==================== 删除方法 ====================

    @Override
    @Transactional
    public boolean deleteByBatchId(Long batchId) {
        if (batchId == null) {
            return false;
        }
        PublishSnapshot snapshot = publishSnapshotMapper.selectByBatchId(batchId);
        if (snapshot != null) {
            snapshot.setDeleted(1);
            return updateById(snapshot);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean forceDeleteByBatchId(Long batchId) {
        if (batchId == null) {
            return false;
        }
        return remove(new LambdaQueryWrapper<PublishSnapshot>().eq(PublishSnapshot::getBatchId, batchId));
    }

    // ==================== 统计方法 ====================

    @Override
    public boolean isPublished(Long batchId) {
        if (batchId == null) {
            return false;
        }
        PublishSnapshot snapshot = publishSnapshotMapper.selectByBatchId(batchId);
        return snapshot != null && snapshot.getDeleted() == 0;
    }

    @Override
    public Integer getRedCount(Long snapshotId) {
        if (snapshotId == null) {
            return 0;
        }
        PublishSnapshot snapshot = getById(snapshotId);
        if (snapshot == null) {
            return 0;
        }
        List<Long> redList = JSON.parseArray(snapshot.getRedList(), Long.class);
        return redList != null ? redList.size() : 0;
    }

    @Override
    public Integer getBlackCount(Long snapshotId) {
        if (snapshotId == null) {
            return 0;
        }
        PublishSnapshot snapshot = getById(snapshotId);
        if (snapshot == null) {
            return 0;
        }
        List<Long> blackList = JSON.parseArray(snapshot.getBlackList(), Long.class);
        return blackList != null ? blackList.size() : 0;
    }

    @Override
    public Integer getParticipantCount(String month, Integer tenantId) {
        if (StringUtils.isBlank(month) || tenantId == null) {
            return 0;
        }
        List<PublishSnapshot> snapshots = publishSnapshotMapper.selectByTenantAndMonth(tenantId, month);
        if (snapshots.isEmpty()) {
            return 0;
        }
        PublishSnapshot snapshot = snapshots.get(0);
        List<PublishSnapshot.SnapshotItem> items = JSON.parseArray(snapshot.getSnapshotData(), PublishSnapshot.SnapshotItem.class);
        return items != null ? items.size() : 0;
    }

    // ==================== 私有辅助方法 ====================

    private SnapshotVO convertToVO(PublishSnapshot snapshot) {
        SnapshotVO vo = new SnapshotVO();
        vo.setBatchId(snapshot.getBatchId());
        vo.setMonth(snapshot.getMonth());
        vo.setPublishTime(snapshot.getPublishTime());
        vo.setPublishBy(snapshot.getPublishBy());
        vo.setPublishByName(snapshot.getPublishByName());

        List<PublishSnapshot.SnapshotItem> items = JSON.parseArray(snapshot.getSnapshotData(), PublishSnapshot.SnapshotItem.class);
        vo.setItems(items);
        vo.setTotalItems(items != null ? items.size() : 0);

        List<Long> redList = JSON.parseArray(snapshot.getRedList(), Long.class);
        List<Long> blackList = JSON.parseArray(snapshot.getBlackList(), Long.class);
        vo.setRedList(redList);
        vo.setBlackList(blackList);
        vo.setRedCount(redList != null ? redList.size() : 0);
        vo.setBlackCount(blackList != null ? blackList.size() : 0);

        return vo;
    }
}