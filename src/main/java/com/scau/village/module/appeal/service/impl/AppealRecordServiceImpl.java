package com.scau.village.module.appeal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.appeal.entity.AppealRecord;
import com.scau.village.module.appeal.mapper.AppealRecordMapper;
import com.scau.village.module.appeal.service.AppealRecordService;
import com.scau.village.module.appeal.vo.AppealVO;
import com.scau.village.module.appeal.vo.AppealDetailVO;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.ScoreEvidenceService;
import com.scau.village.module.rectification.service.RectificationTaskService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 申诉记录服务实现类
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数类型从 Long 改为 String，解决前端精度丢失问题
 * - handleAppeal 中积分恢复同步更新 totalEarnedPoints 和 availablePoints
 * - 修复 apply.getUserId().intValue() 的 NPE 风险
 * - 修复 pointChange 判断逻辑，撤销评分时正确恢复积分
 * - 修复 recordPointsFlow 中 sourceId 类型转换（Integer → String）
 * - pointsApplyMapper.selectById 时使用 Long.parseLong 转换
 *
 * @author system
 * @since 2026-08-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppealRecordServiceImpl extends ServiceImpl<AppealRecordMapper, AppealRecord>
        implements AppealRecordService {

    private final AppealRecordMapper appealRecordMapper;
    private final UserMapper userMapper;
    private final PointsApplyMapper pointsApplyMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final PointsFlowMapper pointsFlowMapper;
    private final ScoreEvidenceService scoreEvidenceService;
    private final RectificationTaskService rectificationTaskService;

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前租户 ID，若为空则抛出异常
     */
    private Integer getTenantId() {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("租户信息缺失，请重新登录");
        }
        return tenantId;
    }

    /**
     * 根据 ID 和租户条件查询申诉记录
     * 修复：参数类型从 Long 改为 String
     */
    private AppealRecord getAppealByIdWithTenant(String appealId) {
        if (appealId == null || appealId.isEmpty()) {
            throw new BusinessException("申诉ID不能为空");
        }
        Integer tenantId = getTenantId();
        LambdaQueryWrapper<AppealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealRecord::getId, appealId)
               .eq(AppealRecord::getTenantId, tenantId);
        AppealRecord record = getOne(wrapper);
        if (record == null) {
            log.warn("申诉记录不存在，appealId={}, tenantId={}", appealId, tenantId);
            throw new BusinessException("申诉记录不存在");
        }
        return record;
    }

    /**
     * 安全获取用户 ID 的 Long 值
     */
    private Long safeGetUserId(PointsApply apply) {
        if (apply == null) {
            return null;
        }
        Object userIdObj = apply.getUserId();
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        return Long.valueOf(userIdObj.toString());
    }

    /**
     * 同步更新用户积分（points / totalEarnedPoints / availablePoints）
     */
    private void updateUserPoints(User user, int changeAmount) {
        if (user == null || changeAmount == 0) {
            return;
        }
        user.setPoints(user.getPoints() + changeAmount);
        user.setTotalEarnedPoints(user.getTotalEarnedPoints() + changeAmount);
        user.setAvailablePoints(user.getAvailablePoints() + changeAmount);
        userMapper.updateById(user);
        log.info("用户积分更新：userId={}, changeAmount={}, 新积分={}, 总获得={}, 可用={}",
                user.getId(), changeAmount, user.getPoints(),
                user.getTotalEarnedPoints(), user.getAvailablePoints());
    }

    /**
     * 记录积分流水（申诉来源）
     * 修复：sourceId 转为 String
     */
    private void recordPointsFlow(User user, int changeAmount, Integer applyId, String remark) {
        if (user == null || changeAmount == 0) {
            return;
        }
        PointsFlow flow = new PointsFlow();
        flow.setUserId(user.getId());
        flow.setChangeAmount(changeAmount);
        flow.setSourceType("appeal");
        flow.setSourceId(String.valueOf(applyId));
        flow.setRemark(remark);
        flow.setCreateTime(LocalDateTime.now());
        flow.setTenantId(user.getTenantId());
        pointsFlowMapper.insert(flow);
    }

    // ==================== 村民端方法 ====================

    /**
     * 提交申诉
     * 修复：applyId 参数类型从 Long 改为 String
     */
    @Override
    @Transactional
    public AppealRecord submitAppeal(String applyId, Long userId, String reason,
                                      String evidencePhotos, Integer tenantId, String batchId) {
        if (applyId == null || userId == null || StringUtils.isBlank(reason)) {
            throw new BusinessException("申诉参数不完整");
        }

        // 1. 检查积分记录是否存在（将 String 转换为 Long）
        PointsApply apply = pointsApplyMapper.selectById(Long.parseLong(applyId));
        if (apply == null) {
            throw new BusinessException("积分记录不存在");
        }

        // 2. 只能申诉自己的扣分记录
        Long applyUserId = safeGetUserId(apply);
        if (applyUserId == null || !applyUserId.equals(userId)) {
            throw new BusinessException("只能申诉自己的积分记录");
        }

        // 3. 只能申诉扣分记录（加分记录不允许申诉）
        PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
        if (rule == null) {
            throw new BusinessException("关联的规则不存在");
        }
        if (rule.getPoints() >= 0) {
            throw new BusinessException("加分记录不允许申诉");
        }

        // 4. 检查是否已经存在待处理的申诉
        if (hasPendingAppeal(applyId)) {
            throw new BusinessException("该记录已存在待处理的申诉，请勿重复提交");
        }

        // 5. 获取用户姓名（冗余存储）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 6. 创建申诉记录（实体字段为 String）
        AppealRecord record = new AppealRecord();
        record.setApplyId(applyId);
        record.setUserId(String.valueOf(userId));
        record.setUserName(user.getRealName());
        record.setReason(reason);
        record.setEvidencePhotos(evidencePhotos);
        record.setStatus(STATUS_PENDING);
        record.setBatchId(batchId);
        record.setTenantId(tenantId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        save(record);
        log.info("用户 {} 提交申诉成功，申诉ID={}, applyId={}", userId, record.getId(), applyId);
        return record;
    }

    @Override
    public Page<AppealVO> getMyAppeals(Long userId, String status, Integer page, Integer size) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Page<AppealRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AppealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealRecord::getUserId, String.valueOf(userId))
                .eq(StringUtils.isNotBlank(status), AppealRecord::getStatus, status)
                .orderByDesc(AppealRecord::getCreateTime);

        Page<AppealRecord> result = appealRecordMapper.selectPage(pageParam, wrapper);
        Page<AppealVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<AppealVO> voList = convertToVOList(result.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取申诉详情（村民端）
     * 修复：appealId 参数类型从 Long 改为 String
     */
    @Override
    public AppealDetailVO getAppealDetail(String appealId, Long userId) {
        if (appealId == null || userId == null) {
            throw new BusinessException("参数不完整");
        }

        Integer tenantId = getTenantId();

        LambdaQueryWrapper<AppealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealRecord::getId, appealId)
               .eq(AppealRecord::getTenantId, tenantId);
        AppealRecord record = getOne(wrapper);

        if (record == null) {
            throw new BusinessException("申诉记录不存在");
        }
        // 权限校验：只能查看自己的申诉
        if (!record.getUserId().equals(String.valueOf(userId))) {
            throw new BusinessException("无权查看此申诉");
        }
        return convertToDetailVO(record);
    }

    @Override
    public List<AppealVO> getMyAppealCounts(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        List<AppealVO> counts = new ArrayList<>();
        counts.add(createCountVO(STATUS_PENDING, appealRecordMapper.countByUserIdAndStatus(String.valueOf(userId), STATUS_PENDING)));
        counts.add(createCountVO(STATUS_RESOLVED, appealRecordMapper.countByUserIdAndStatus(String.valueOf(userId), STATUS_RESOLVED)));
        return counts;
    }

    private AppealVO createCountVO(String status, Long count) {
        AppealVO vo = new AppealVO();
        vo.setStatus(status);
        vo.setCount(count);
        return vo;
    }

    // ==================== 管理员端方法 ====================

    @Override
    public Page<AppealVO> getAdminAppeals(Integer tenantId, String status, Integer page, Integer size) {
        if (tenantId == null) {
            throw new BusinessException("租户ID不能为空");
        }
        Page<AppealRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AppealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealRecord::getTenantId, tenantId)
                .eq(StringUtils.isNotBlank(status), AppealRecord::getStatus, status)
                .orderByDesc(AppealRecord::getCreateTime);

        Page<AppealRecord> result = appealRecordMapper.selectPage(pageParam, wrapper);
        Page<AppealVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<AppealVO> voList = convertToVOList(result.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 管理员获取申诉详情
     * 修复：appealId 参数类型从 Long 改为 String
     */
    @Override
    public AppealDetailVO getAdminAppealDetail(String appealId) {
        if (appealId == null || appealId.isEmpty()) {
            throw new BusinessException("申诉ID不能为空");
        }

        Integer tenantId = getTenantId();

        LambdaQueryWrapper<AppealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppealRecord::getId, appealId)
               .eq(AppealRecord::getTenantId, tenantId);
        AppealRecord record = getOne(wrapper);

        if (record == null) {
            log.warn("【管理员-申诉详情】申诉记录不存在，appealId={}, tenantId={}", appealId, tenantId);
            throw new BusinessException("申诉记录不存在");
        }

        log.info("【管理员-申诉详情】查询成功，appealId={}, status={}", appealId, record.getStatus());
        return convertToDetailVO(record);
    }

    @Override
    public List<AppealRecord> getPendingAppeals(Integer tenantId) {
        if (tenantId == null) {
            throw new BusinessException("租户ID不能为空");
        }
        return appealRecordMapper.selectPendingAppeals(tenantId);
    }

    /**
     * 管理员处理申诉
     * 修复：appealId 参数类型从 Long 改为 String
     */
    @Override
    @Transactional
    public AppealRecord handleAppeal(String appealId, Long reviewerId, String decision,
                                      String decisionDetail, Integer newPoints) {
        if (appealId == null || reviewerId == null || StringUtils.isBlank(decision)) {
            throw new BusinessException("处理参数不完整");
        }

        // 1. 带租户条件查询申诉记录
        AppealRecord record = getAppealByIdWithTenant(appealId);

        if (!STATUS_PENDING.equals(record.getStatus())) {
            throw new BusinessException("该申诉已处理，请勿重复操作");
        }

        log.info("【处理申诉】查询到申诉记录，appealId={}, applyId={}, status={}",
                appealId, record.getApplyId(), record.getStatus());

        // 2. 获取复核人信息
        User reviewer = userMapper.selectById(reviewerId);
        if (reviewer == null) {
            throw new BusinessException("复核人不存在");
        }

        // 3. 查询关联的积分记录（record.getApplyId() 是 String，转为 Long）
        PointsApply apply = pointsApplyMapper.selectById(Long.parseLong(record.getApplyId()));
        if (apply == null) {
            throw new BusinessException("关联的积分记录不存在");
        }

        // 4. 查询原规则获取分值
        PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
        if (rule == null) {
            throw new BusinessException("关联的规则不存在");
        }
        int originalPoints = rule.getPoints(); // 负数（扣分）

        // 5. 根据决定执行不同操作
        boolean needUpdatePoints = false;
        int finalPoints = originalPoints; // 最终分值（负数或0）
        String flowRemark = "";

        if (DECISION_UPHELD.equals(decision)) {
            // 维持原判：不做任何修改
            log.info("申诉维持原判，appealId={}, applyId={}", appealId, record.getApplyId());
        } else if (DECISION_MODIFIED.equals(decision)) {
            // 修改评分：调整分值（newPoints 是扣分值，如 -3）
            if (newPoints == null) {
                throw new BusinessException("修改评分时必须指定新的分值");
            }
            if (newPoints >= 0) {
                throw new BusinessException("修改评分时新分值应为负数（表示扣分）");
            }
            if (originalPoints == newPoints) {
                throw new BusinessException("新分值不能与原分值相同");
            }
            needUpdatePoints = true;
            finalPoints = newPoints;
            flowRemark = String.format("申诉修改评分：原扣%d分，改为%d分",
                    Math.abs(originalPoints), Math.abs(finalPoints));
            log.info("申诉修改评分，appealId={}, applyId={}, 原分值={}, 新分值={}",
                    appealId, record.getApplyId(), originalPoints, finalPoints);
        } else if (DECISION_REVOKED.equals(decision)) {
            // 撤销评分：完全撤销扣分（分值变为0）
            needUpdatePoints = true;
            finalPoints = 0;
            flowRemark = String.format("申诉撤销评分：原扣%d分，已全部恢复",
                    Math.abs(originalPoints));
            log.info("申诉撤销评分，appealId={}, applyId={}", appealId, record.getApplyId());
        } else {
            throw new BusinessException("无效的复核决定，请使用 upheld / modified / revoked");
        }

        // 6. 如果需要更新积分
        if (needUpdatePoints) {
            int change = originalPoints - finalPoints;

            if (change < 0) {
                int increaseAmount = Math.abs(change);
                User user = userMapper.selectById(apply.getUserId());
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }

                // 同步更新三个积分字段
                updateUserPoints(user, increaseAmount);

                // 记录积分流水
                recordPointsFlow(user, increaseAmount, apply.getId(), flowRemark);

                log.info("【处理申诉】用户积分恢复，userId={}, 恢复{}分, 原因={}",
                        user.getId(), increaseAmount, decision);

                // 更新 points_apply 的备注
                apply.setAuditRemark(flowRemark);
                pointsApplyMapper.updateById(apply);

                // 如果是撤销评分，删除整改任务和证据（record.getApplyId() 是 String）
                if (DECISION_REVOKED.equals(decision)) {
                    rectificationTaskService.updateStatusByApplyId(record.getApplyId(), "deleted");
                    // scoreEvidenceService.deleteByApplyId 可能需要 String，保持统一
                    scoreEvidenceService.deleteByApplyId(record.getApplyId());
                    log.info("【处理申诉】撤销评分，已删除整改任务和证据，applyId={}", record.getApplyId());
                }
            } else if (change > 0) {
                log.error("【处理申诉】异常：申诉处理导致扣分加重，originalPoints={}, finalPoints={}, change={}",
                        originalPoints, finalPoints, change);
                throw new BusinessException("申诉处理逻辑错误：不会加重处罚");
            }
        }

        // 7. 更新申诉记录
        record.setStatus(STATUS_RESOLVED);
        record.setDecision(decision);
        record.setDecisionDetail(decisionDetail);
        record.setReviewerId(String.valueOf(reviewerId));
        record.setReviewerName(reviewer.getRealName());
        record.setReviewTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);

        log.info("管理员 {} 处理申诉完成，appealId={}, decision={}", reviewerId, appealId, decision);
        return record;
    }

    // ==================== 统计方法 ====================

    @Override
    public Long countByUserIdAndStatus(Long userId, String status) {
        if (userId == null || StringUtils.isBlank(status)) {
            return 0L;
        }
        return appealRecordMapper.countByUserIdAndStatus(String.valueOf(userId), status);
    }

    /**
     * 根据批次ID统计申诉数量
     * 修复：batchId 参数类型从 Long 改为 String
     */
    @Override
    public Long countByBatchId(String batchId) {
        if (batchId == null || batchId.isEmpty()) {
            return 0L;
        }
        return appealRecordMapper.countByBatchId(batchId);
    }

    /**
     * 检查某条积分记录是否存在申诉（任意状态）
     * 修复：applyId 参数类型从 Long 改为 String
     */
    @Override
    public boolean existsByApplyId(String applyId) {
        if (applyId == null || applyId.isEmpty()) {
            return false;
        }
        AppealRecord record = appealRecordMapper.selectByApplyId(applyId);
        return record != null && !record.getDeleted().equals(1);
    }

    /**
     * 检查某条积分记录是否存在待处理的申诉
     * 修复：applyId 参数类型从 Long 改为 String
     */
    @Override
    public boolean hasPendingAppeal(String applyId) {
        if (applyId == null || applyId.isEmpty()) {
            return false;
        }
        AppealRecord record = appealRecordMapper.selectByApplyId(applyId);
        return record != null && STATUS_PENDING.equals(record.getStatus());
    }

    // ==================== 私有辅助方法 ====================

    private List<AppealVO> convertToVOList(List<AppealRecord> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<AppealVO> voList = new ArrayList<>();
        for (AppealRecord record : records) {
            voList.add(convertToVO(record));
        }
        return voList;
    }

    private AppealVO convertToVO(AppealRecord record) {
        AppealVO vo = new AppealVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private AppealDetailVO convertToDetailVO(AppealRecord record) {
        AppealDetailVO vo = new AppealDetailVO();
        BeanUtils.copyProperties(record, vo);
        // record.getApplyId() 是 String，转为 Long 查询
        if (record.getApplyId() != null) {
            PointsApply apply = pointsApplyMapper.selectById(Long.parseLong(record.getApplyId()));
            if (apply != null) {
                vo.setApplyStatus(apply.getStatus());
                vo.setApplyDescription(apply.getDescription());
                vo.setApplyImages(apply.getImages());
                PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
                if (rule != null) {
                    vo.setRuleName(rule.getRuleName());
                    vo.setRulePoints(rule.getPoints());
                }
            }
        }
        return vo;
    }
}