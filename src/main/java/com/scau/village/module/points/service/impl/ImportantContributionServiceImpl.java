package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.ImportantContribution;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.mapper.ImportantContributionMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.ImportantContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 重要贡献认定服务实现。 */
@Service
@RequiredArgsConstructor
public class ImportantContributionServiceImpl
        extends ServiceImpl<ImportantContributionMapper, ImportantContribution>
        implements ImportantContributionService {

    private final PointsRuleMapper pointsRuleMapper;

    @Override
    @Transactional
    public ImportantContribution submit(int userId, int ruleId, String contributionDesc,
                                        String evidencePhotos, String sourceRef) {
        String contributionType = contributionType(ruleId);
        PointsRule rule = pointsRuleMapper.selectById(ruleId);
        if (rule == null || !"important".equals(rule.getBehaviorType())) {
            throw new BusinessException("该规则不是重要贡献规则");
        }
        ImportantContribution contribution = new ImportantContribution();
        contribution.setTenantId(UserContext.getCurrentTenantId());
        contribution.setUserId(userId);
        contribution.setRuleId(ruleId);
        contribution.setRuleVersion(rule.getRuleVersion() == null ? "1.0" : rule.getRuleVersion());
        contribution.setContributionType(contributionType);
        contribution.setContributionDesc(contributionDesc);
        contribution.setPoints(rule.getPoints());
        contribution.setStatus(ImportantContribution.STATUS_PENDING);
        contribution.setEvidencePhotos(evidencePhotos);
        contribution.setSourceRef(sourceRef);
        contribution.setCreateTime(LocalDateTime.now());
        save(contribution);
        return contribution;
    }

    @Override
    public ImportantContribution requireApproved(int userId, int ruleId, String contributionId) {
        if (contributionId == null || contributionId.trim().isEmpty()) {
            throw new BusinessException("重要贡献认定未通过或不存在");
        }
        ImportantContribution contribution = getById(contributionId);
        if (contribution == null || !Integer.valueOf(userId).equals(contribution.getUserId())
                || !Integer.valueOf(ruleId).equals(contribution.getRuleId())
                || !ImportantContribution.STATUS_APPROVED.equals(contribution.getStatus())) {
            throw new BusinessException("重要贡献认定未通过或不存在");
        }
        return contribution;
    }

    @Override
    @Transactional
    public void approve(String contributionId, int approverId, String remark) {
        ImportantContribution contribution = getById(contributionId);
        if (contribution == null || !ImportantContribution.STATUS_PENDING.equals(contribution.getStatus())) {
            throw new BusinessException("重要贡献记录不存在或已处理");
        }
        contribution.setStatus(ImportantContribution.STATUS_APPROVED);
        contribution.setApprovedBy(approverId);
        contribution.setApprovedTime(LocalDateTime.now());
        contribution.setRemark(remark);
        updateById(contribution);
    }

    @Override
    public List<ImportantContribution> listPending(int tenantId) {
        LambdaQueryWrapper<ImportantContribution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportantContribution::getTenantId, tenantId)
                .eq(ImportantContribution::getStatus, ImportantContribution.STATUS_PENDING)
                .orderByAsc(ImportantContribution::getCreateTime);
        return list(wrapper);
    }

    private String contributionType(int ruleId) {
        if (ruleId == 18) {
            return "care";
        }
        if (ruleId == 21) {
            return "mediate";
        }
        if (ruleId == 28) {
            return "suggestion";
        }
        throw new BusinessException("不支持的重要贡献规则");
    }
}
