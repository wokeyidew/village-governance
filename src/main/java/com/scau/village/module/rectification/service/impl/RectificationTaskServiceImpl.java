package com.scau.village.module.rectification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.rectification.entity.RectificationTask;
import com.scau.village.module.rectification.mapper.RectificationTaskMapper;
import com.scau.village.module.rectification.service.RectificationTaskService;
import com.scau.village.module.rectification.vo.RectificationTaskVO;
import com.scau.village.module.rectification.vo.RectificationDetailVO;
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
 * 整改任务服务实现类
 *
 * @author system
 * @since 2026-08-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RectificationTaskServiceImpl extends ServiceImpl<RectificationTaskMapper, RectificationTask>
        implements RectificationTaskService {

    private final RectificationTaskMapper rectificationTaskMapper;
    private final UserMapper userMapper;
    private final PointsApplyMapper pointsApplyMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final PointsFlowMapper pointsFlowMapper;

    // ==================== 创建方法 ====================

    @Override
    @Transactional
    public RectificationTask createTask(Long applyId, Long userId, Long batchId, String ruleName,
                                        String requirement, LocalDateTime deadline,
                                        String beforePhotos, Long inspectorId) {
        if (applyId == null || userId == null) {
            throw new BusinessException("创建整改任务失败：缺少必要参数");
        }

        // 检查是否已存在关联的整改任务（防止重复创建）
        RectificationTask existing = rectificationTaskMapper.selectByApplyId(applyId);
        if (existing != null) {
            log.warn("积分记录 {} 已存在整改任务，跳过创建", applyId);
            return existing;
        }

        // 获取用户信息（用于冗余存储姓名）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        RectificationTask task = new RectificationTask();
        task.setApplyId(applyId);
        task.setUserId(userId);
        task.setBatchId(batchId);
        task.setRuleName(ruleName);
        task.setRequirement(requirement);
        task.setDeadline(deadline);
        task.setBeforePhotos(beforePhotos);
        task.setInspectorId(inspectorId);
        task.setStatus(STATUS_PENDING);
        task.setTenantId(user.getTenantId());
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        save(task);
        log.info("创建整改任务成功，taskId={}, applyId={}, userId={}", task.getId(), applyId, userId);
        return task;
    }

    // ==================== 村民端查询方法 ====================

    @Override
    public Page<RectificationTaskVO> getMyTasks(Long userId, String status, Integer page, Integer size) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Page<RectificationTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RectificationTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RectificationTask::getUserId, userId)
                .eq(StringUtils.isNotBlank(status), RectificationTask::getStatus, status)
                .orderByDesc(RectificationTask::getCreateTime);

        Page<RectificationTask> result = rectificationTaskMapper.selectPage(pageParam, wrapper);
        Page<RectificationTaskVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<RectificationTaskVO> voList = convertToVOList(result.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<RectificationTaskVO> getMyTaskCounts(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        List<RectificationTaskVO> counts = new ArrayList<>();
        // 各状态计数
        counts.add(createCountVO(STATUS_PENDING, rectificationTaskMapper.countByUserIdAndStatus(userId, STATUS_PENDING)));
        counts.add(createCountVO(STATUS_REVIEWING, rectificationTaskMapper.countByUserIdAndStatus(userId, STATUS_REVIEWING)));
        counts.add(createCountVO(STATUS_RESOLVED, rectificationTaskMapper.countByUserIdAndStatus(userId, STATUS_RESOLVED)));
        counts.add(createCountVO(STATUS_OVERDUE, rectificationTaskMapper.countByUserIdAndStatus(userId, STATUS_OVERDUE)));
        return counts;
    }

    private RectificationTaskVO createCountVO(String status, Long count) {
        RectificationTaskVO vo = new RectificationTaskVO();
        vo.setStatus(status);
        vo.setCount(count);
        return vo;
    }

    @Override
    public RectificationDetailVO getTaskDetail(Long taskId, Long userId) {
        // 🔥 增加日志：记录入参
        log.info("【整改详情-村民端】开始查询，taskId={}, userId={}", taskId, userId);

        if (taskId == null || userId == null) {
            log.warn("【整改详情-村民端】参数不完整，taskId={}, userId={}", taskId, userId);
            throw new BusinessException("参数不完整");
        }

        // 直接使用 MyBatis-Plus 的 getById 查询（不会自动添加 tenant_id 过滤）
        RectificationTask task = getById(taskId);

        // 🔥 增加日志：记录查询结果
        if (task == null) {
            log.warn("【整改详情-村民端】查询结果为空，taskId={}，数据库中不存在该记录", taskId);
            throw new BusinessException("整改任务不存在，taskId=" + taskId);
        }

        log.info("【整改详情-村民端】查询到记录，taskId={}, userId={}, status={}, applyId={}",
                task.getId(), task.getUserId(), task.getStatus(), task.getApplyId());

        // 权限校验：只能查看自己的任务
        if (!task.getUserId().equals(userId)) {
            log.warn("【整改详情-村民端】权限校验失败，taskId={}, 任务所属用户={}, 当前用户={}",
                    taskId, task.getUserId(), userId);
            throw new BusinessException("无权查看此任务");
        }

        log.info("【整改详情-村民端】权限校验通过，taskId={}", taskId);
        return convertToDetailVO(task);
    }

    // ==================== 村民端操作方法 ====================

    @Override
    @Transactional
    public RectificationTask submitRectification(Long taskId, Long userId, String afterPhotos, String submitRemark) {
        if (taskId == null || userId == null) {
            throw new BusinessException("参数不完整");
        }
        RectificationTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("整改任务不存在");
        }
        // 权限校验
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此任务");
        }
        // 状态校验：只有待整改或待复核状态可以提交
        if (!STATUS_PENDING.equals(task.getStatus()) && !STATUS_REVIEWING.equals(task.getStatus())) {
            throw new BusinessException("当前状态不可提交整改");
        }
        // 图片校验
        if (StringUtils.isBlank(afterPhotos)) {
            throw new BusinessException("请上传整改后的照片");
        }

        task.setAfterPhotos(afterPhotos);
        task.setSubmitRemark(submitRemark);
        task.setSubmitTime(LocalDateTime.now());
        task.setStatus(STATUS_REVIEWING);
        task.setUpdateTime(LocalDateTime.now());
        updateById(task);
        log.info("村民提交整改成功，taskId={}, userId={}", taskId, userId);
        return task;
    }

    // ==================== 管理员端查询方法 ====================

    @Override
    public Page<RectificationTaskVO> getAdminTaskList(Integer tenantId, String status, Integer page, Integer size) {
        if (tenantId == null) {
            throw new BusinessException("租户ID不能为空");
        }
        Page<RectificationTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RectificationTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RectificationTask::getTenantId, tenantId)
                .eq(StringUtils.isNotBlank(status), RectificationTask::getStatus, status)
                .orderByDesc(RectificationTask::getCreateTime);

        Page<RectificationTask> result = rectificationTaskMapper.selectPage(pageParam, wrapper);
        Page<RectificationTaskVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<RectificationTaskVO> voList = convertToVOList(result.getRecords());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public RectificationDetailVO getAdminTaskDetail(Long taskId) {
        // 🔥 增加日志：记录入参
        log.info("【整改详情-管理员端】开始查询，taskId={}", taskId);

        if (taskId == null) {
            log.warn("【整改详情-管理员端】taskId 为空");
            throw new BusinessException("任务ID不能为空");
        }

        // 直接使用 MyBatis-Plus 的 getById 查询（不会自动添加 tenant_id 过滤）
        RectificationTask task = getById(taskId);

        // 🔥 增加日志：记录查询结果
        if (task == null) {
            log.warn("【整改详情-管理员端】查询结果为空，taskId={}，数据库中不存在该记录", taskId);
            throw new BusinessException("整改任务不存在，taskId=" + taskId);
        }

        log.info("【整改详情-管理员端】查询到记录，taskId={}, userId={}, status={}, applyId={}",
                task.getId(), task.getUserId(), task.getStatus(), task.getApplyId());

        return convertToDetailVO(task);
    }

    @Override
    public List<RectificationTask> getTasksByBatchId(Long batchId, String status) {
        if (batchId == null) {
            throw new BusinessException("批次ID不能为空");
        }
        if (StringUtils.isNotBlank(status)) {
            return rectificationTaskMapper.selectByBatchIdAndStatus(batchId, status);
        } else {
            return rectificationTaskMapper.selectByBatchId(batchId);
        }
    }

    // ==================== 管理员端操作方法 ====================

    @Override
    @Transactional
    public RectificationTask reviewTask(Long taskId, Long reviewerId, String reviewResult, String reviewRemark) {
        if (taskId == null || reviewerId == null || StringUtils.isBlank(reviewResult)) {
            throw new BusinessException("参数不完整");
        }
        RectificationTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("整改任务不存在");
        }
        // 状态校验：只有待复核状态可以复核
        if (!STATUS_REVIEWING.equals(task.getStatus())) {
            throw new BusinessException("当前状态不可复核，请确认任务已提交整改");
        }

        // 复核通过：恢复积分（扣分值的50%）
        if (REVIEW_PASSED.equals(reviewResult)) {
            // 查询积分记录
            PointsApply apply = pointsApplyMapper.selectById(task.getApplyId());
            if (apply == null) {
                throw new BusinessException("关联的积分记录不存在");
            }
            // 查询规则获取扣分值
            PointsRule rule = pointsRuleMapper.selectById(apply.getRuleId());
            if (rule == null) {
                throw new BusinessException("关联的规则不存在");
            }
            int penaltyPoints = Math.abs(rule.getPoints());
            int rewardPoints = penaltyPoints / 2; // 恢复50%
            if (rewardPoints > 0) {
                // 更新用户积分
                User user = userMapper.selectById(task.getUserId());
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                user.setPoints(user.getPoints() + rewardPoints);
                userMapper.updateById(user);

                // 记录积分流水
                PointsFlow flow = new PointsFlow();
                flow.setUserId(user.getId());
                flow.setChangeAmount(rewardPoints);
                flow.setSourceType("rectification");
                flow.setSourceId(task.getApplyId().intValue());
                flow.setRemark("整改奖励积分（扣分" + penaltyPoints + "分，恢复" + rewardPoints + "分）");
                flow.setCreateTime(LocalDateTime.now());
                pointsFlowMapper.insert(flow);
            }

            task.setStatus(STATUS_RESOLVED);
            task.setRewardPoints(rewardPoints);
            log.info("整改复核通过，taskId={}, 奖励积分={}", taskId, rewardPoints);
        } else if (REVIEW_REJECTED.equals(reviewResult)) {
            // 复核不通过：退回待整改
            task.setStatus(STATUS_PENDING);
            log.info("整改复核不通过，taskId={}, 退回待整改", taskId);
        } else {
            throw new BusinessException("复核结果参数错误，请使用 'passed' 或 'rejected'");
        }

        task.setReviewerId(reviewerId);
        task.setReviewRemark(reviewRemark);
        task.setReviewTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        updateById(task);
        return task;
    }

    // ==================== 统计方法 ====================

    @Override
    public Long countByUserIdAndStatus(Long userId, String status) {
        if (userId == null || StringUtils.isBlank(status)) {
            return 0L;
        }
        return rectificationTaskMapper.countByUserIdAndStatus(userId, status);
    }

    @Override
    public Long countByBatchId(Long batchId) {
        if (batchId == null) {
            return 0L;
        }
        return rectificationTaskMapper.countByBatchId(batchId);
    }

    @Override
    public Integer calculateCompletionRate(Long batchId) {
        if (batchId == null) {
            return 0;
        }
        return rectificationTaskMapper.calculateCompletionRate(batchId);
    }

    @Override
    public Long countPendingTasks(Long userId) {
        if (userId == null) {
            return 0L;
        }
        // 待整改 = pending + reviewing（待复核也视为未完成，因为村民已提交但管理员未处理）
        long pending = rectificationTaskMapper.countByUserIdAndStatus(userId, STATUS_PENDING);
        long reviewing = rectificationTaskMapper.countByUserIdAndStatus(userId, STATUS_REVIEWING);
        return pending + reviewing;
    }

    // ==================== 定时任务方法 ====================

    @Override
    @Transactional
    public int batchUpdateOverdueStatus() {
        int updated = rectificationTaskMapper.batchUpdateOverdueStatus(STATUS_OVERDUE, STATUS_PENDING);
        if (updated > 0) {
            log.info("定时任务：将 {} 条逾期任务标记为逾期", updated);
        }
        return updated;
    }

    @Override
    @Transactional
    public int updateStatusByApplyId(Long applyId, String status) {
        if (applyId == null || StringUtils.isBlank(status)) {
            return 0;
        }
        return rectificationTaskMapper.updateStatusByApplyId(applyId, status);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将实体列表转换为 VO 列表
     */
    private List<RectificationTaskVO> convertToVOList(List<RectificationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }
        List<RectificationTaskVO> voList = new ArrayList<>();
        for (RectificationTask task : tasks) {
            voList.add(convertToVO(task));
        }
        return voList;
    }

    /**
     * 将实体转换为 VO
     */
    private RectificationTaskVO convertToVO(RectificationTask task) {
        RectificationTaskVO vo = new RectificationTaskVO();
        BeanUtils.copyProperties(task, vo);
        // 补充用户姓名（可选）
        User user = userMapper.selectById(task.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
        }
        return vo;
    }

    /**
     * 将实体转换为详情 VO
     */
    private RectificationDetailVO convertToDetailVO(RectificationTask task) {
        RectificationDetailVO vo = new RectificationDetailVO();
        BeanUtils.copyProperties(task, vo);
        // 补充用户信息
        User user = userMapper.selectById(task.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
            vo.setUserPhone(user.getPhone());
        }
        // 补充检查人信息
        if (task.getInspectorId() != null) {
            User inspector = userMapper.selectById(task.getInspectorId());
            if (inspector != null) {
                vo.setInspectorName(inspector.getRealName());
            }
        }
        // 补充复核人信息
        if (task.getReviewerId() != null) {
            User reviewer = userMapper.selectById(task.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getRealName());
            }
        }
        // 计算是否逾期
        vo.setOverdue(task.isOverdue());
        return vo;
    }
}