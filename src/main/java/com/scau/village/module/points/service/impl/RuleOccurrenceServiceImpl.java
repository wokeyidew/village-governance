package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.module.points.entity.RuleOccurrence;
import com.scau.village.module.points.mapper.RuleOccurrenceMapper;
import com.scau.village.module.points.service.RuleOccurrenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 规则族发生记录服务实现。 */
@Service
@RequiredArgsConstructor
public class RuleOccurrenceServiceImpl extends ServiceImpl<RuleOccurrenceMapper, RuleOccurrence>
        implements RuleOccurrenceService {

    @Override
    public int nextOccurrenceNo(int userId, String familyCode) {
        // TODO: 需人工确认并发评分时 occurrence 序号的锁定策略。
        LambdaQueryWrapper<RuleOccurrence> wrapper = baseQuery(userId, familyCode)
                .eq(RuleOccurrence::getCancelled, 0);
        return Math.toIntExact(count(wrapper) + 1);
    }

    @Override
    @Transactional
    public RuleOccurrence record(int userId, String familyCode, int occurrenceNo,
                                 String applyId, String ruleVersion) {
        if (familyCode == null || familyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("规则族编码不能为空");
        }
        if (applyId == null || applyId.trim().isEmpty()) {
            throw new IllegalArgumentException("来源申报ID不能为空");
        }
        RuleOccurrence occurrence = new RuleOccurrence();
        occurrence.setTenantId(UserContext.getCurrentTenantId());
        occurrence.setUserId(userId);
        occurrence.setFamilyCode(familyCode);
        occurrence.setOccurrenceNo(occurrenceNo);
        occurrence.setEventTime(LocalDateTime.now());
        occurrence.setSourceApplyId(applyId);
        occurrence.setRuleVersion(ruleVersion == null || ruleVersion.trim().isEmpty()
                ? "1.0" : ruleVersion);
        occurrence.setIdempotencyKey(familyCode + ":" + applyId);
        occurrence.setCancelled(0);
        save(occurrence);
        return occurrence;
    }

    @Override
    public List<RuleOccurrence> listByUser(int userId, String familyCode) {
        return list(baseQuery(userId, familyCode).orderByAsc(RuleOccurrence::getEventTime)
                .orderByAsc(RuleOccurrence::getId));
    }

    private LambdaQueryWrapper<RuleOccurrence> baseQuery(int userId, String familyCode) {
        return new LambdaQueryWrapper<RuleOccurrence>()
                .eq(RuleOccurrence::getTenantId, UserContext.getCurrentTenantId())
                .eq(RuleOccurrence::getUserId, userId)
                .eq(RuleOccurrence::getFamilyCode, familyCode);
    }
}
