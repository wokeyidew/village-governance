package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.module.points.entity.InspectionHousehold;
import com.scau.village.module.points.mapper.InspectionHouseholdMapper;
import com.scau.village.module.points.service.InspectionHouseholdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 检查户汇总服务实现类
 *
 * 修复说明（2026-08-30）：
 * - batchId 从 Long 改为 String，对应实体类字段类型
 * - inspectorId 从 Integer 改为 String，对应实体类字段类型
 * - 适配 selectByBatchIdAndUserId 方法的参数变化
 * - 【核心修复】新增记录时显式生成雪花 ID（IdWorker.getIdStr()）
 * - 【核心修复】DuplicateKeyException 不再吞掉异常，改为重新抛出，确保事务回滚
 *
 * @author system
 * @since 2026-07-16
 */
@Slf4j
@Service
public class InspectionHouseholdServiceImpl
        extends ServiceImpl<InspectionHouseholdMapper, InspectionHousehold>
        implements InspectionHouseholdService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionHousehold saveOrUpdateSummary(String batchId, Integer userId, Integer totalScore,
                                                   String detailJson, String inspectorId, String remark) {
        // 获取租户ID，若为空则使用默认值 1
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            tenantId = 1; // 默认租户ID（龙胜村）
            log.warn("UserContext 中 tenantId 为空，使用默认值: 1");
        }

        // 转换 userId 为 Long（实体类 userId 为 Long）
        Long userIdLong = userId != null ? userId.longValue() : null;

        log.info("【户汇总表】开始保存/更新，batchId={}, userId={}, totalScore={}, inspectorId={}",
                batchId, userId, totalScore, inspectorId);

        // 查询是否已存在汇总记录（Mapper 方法参数适应：batchId String, userId Integer）
        InspectionHousehold existing = null;
        try {
            existing = baseMapper.selectByBatchIdAndUserId(batchId, userId);
        } catch (Exception e) {
            log.error("查询汇总记录失败，batchId={}, userId={}", batchId, userId, e);
            // 如果查询失败，视为不存在，继续执行插入
        }

        if (existing != null) {
            // 更新已有记录
            existing.setTotalScore(totalScore);
            if (detailJson != null) {
                existing.setDetailJson(detailJson);
            }
            if (remark != null) {
                existing.setRemark(remark);
            }
            existing.setInspectorId(inspectorId);
            existing.setUpdateTime(LocalDateTime.now());
            updateById(existing);
            log.info("【户汇总表】更新汇总记录成功，batchId={}, userId={}, totalScore={}, id={}",
                    batchId, userId, totalScore, existing.getId());
            return existing;
        } else {
            // 新增记录
            InspectionHousehold household = new InspectionHousehold();
            // ================================================================
            // 【核心修复】显式生成雪花 ID（String 类型）
            // 使用 MyBatis-Plus 的 IdWorker 生成 19 位雪花 ID
            // ================================================================
            household.setId(IdWorker.getIdStr());
            household.setBatchId(batchId);
            household.setUserId(userIdLong);
            household.setTotalScore(totalScore);
            household.setDetailJson(detailJson);
            household.setInspectorId(inspectorId);
            household.setRemark(remark);
            // 确保 tenantId 不为 null，数据库表 tenant_id 为 NOT NULL
            household.setTenantId(tenantId.longValue());
            household.setCreateTime(LocalDateTime.now());
            household.setUpdateTime(LocalDateTime.now());
            household.setDeleted(0);

            try {
                save(household);
                log.info("【户汇总表】新增汇总记录成功，batchId={}, userId={}, totalScore={}, id={}",
                        batchId, userId, totalScore, household.getId());
                return household;
            } catch (DuplicateKeyException e) {
                // ================================================================
                // 【核心修复】不再吞掉异常，重新抛出
                // 并发冲突时直接抛出异常，让外层事务回滚，前端会收到明确的错误提示
                // 不再执行静默的"转为更新"逻辑，因为这样会导致数据不一致
                // ================================================================
                log.error("【户汇总表】插入记录唯一键冲突，batchId={}, userId={}, 错误: {}",
                        batchId, userId, e.getMessage(), e);
                throw new RuntimeException("汇总记录已存在，批次ID=" + batchId + ", 用户ID=" + userId, e);
            }
        }
    }
}