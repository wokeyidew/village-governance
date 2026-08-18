package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.ScoreEvidence;
import com.scau.village.module.points.mapper.ScoreEvidenceMapper;
import com.scau.village.module.points.service.ScoreEvidenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评分证据服务实现类
 *
 * @author system
 * @since 2026-08-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreEvidenceServiceImpl extends ServiceImpl<ScoreEvidenceMapper, ScoreEvidence>
        implements ScoreEvidenceService {

    private final ScoreEvidenceMapper scoreEvidenceMapper;

    @Override
    public ScoreEvidence getByApplyId(Long applyId) {
        if (applyId == null) {
            return null;
        }
        return scoreEvidenceMapper.selectByApplyId(applyId);
    }

    @Override
    public ScoreEvidence getByApplyIdAndTenant(Long applyId, Integer tenantId) {
        if (applyId == null || tenantId == null) {
            return null;
        }
        // 直接查询证据，租户隔离由上层通过 applyId 关联 points_apply 表校验
        // 这里简化返回，实际业务中应由调用方保证租户隔离
        return scoreEvidenceMapper.selectByApplyId(applyId);
    }

    @Override
    public List<ScoreEvidence> listByApplyIds(List<Long> applyIds) {
        if (applyIds == null || applyIds.isEmpty()) {
            return List.of();
        }
        return scoreEvidenceMapper.selectByApplyIds(applyIds);
    }

    @Override
    public List<ScoreEvidence> listByBatchId(Long batchId) {
        if (batchId == null) {
            return List.of();
        }
        return scoreEvidenceMapper.selectByBatchId(batchId);
    }

    @Override
    public List<ScoreEvidence> listByInspectorId(Long inspectorId) {
        if (inspectorId == null) {
            return List.of();
        }
        return scoreEvidenceMapper.selectByInspectorId(inspectorId);
    }

    @Override
    @Transactional
    public ScoreEvidence saveEvidence(Long applyId, String photoUrls, String location,
                                      Long inspectorId, Long batchId, String ruleVersion,
                                      String ruleName, String userName, Integer hasWatermark) {
        if (applyId == null) {
            throw new BusinessException("积分记录ID不能为空");
        }
        if (StringUtils.isBlank(photoUrls)) {
            throw new BusinessException("证据照片不能为空");
        }

        // 检查是否已存在证据，若存在则更新
        ScoreEvidence existing = scoreEvidenceMapper.selectByApplyId(applyId);
        if (existing != null) {
            // 更新：追加照片、更新位置、检查人等
            existing.setPhotoUrls(photoUrls);
            existing.setPhotoCount(photoUrls.split(",").length);
            if (location != null) {
                existing.setLocation(location);
            }
            if (inspectorId != null) {
                existing.setInspectorId(inspectorId);
            }
            if (batchId != null) {
                existing.setBatchId(batchId);
            }
            if (ruleVersion != null) {
                existing.setRuleVersion(ruleVersion);
            }
            if (ruleName != null) {
                existing.setRuleName(ruleName);
            }
            if (userName != null) {
                existing.setUserName(userName);
            }
            if (hasWatermark != null) {
                existing.setHasWatermark(hasWatermark);
            }
            updateById(existing);
            log.info("更新证据成功，applyId={}, photoCount={}", applyId, existing.getPhotoCount());
            return existing;
        } else {
            // 新增
            ScoreEvidence evidence = new ScoreEvidence();
            evidence.setApplyId(applyId);
            evidence.setPhotoUrls(photoUrls);
            evidence.setPhotoCount(photoUrls.split(",").length);
            evidence.setLocation(location);
            evidence.setInspectorId(inspectorId);
            evidence.setBatchId(batchId);
            evidence.setRuleVersion(ruleVersion);
            evidence.setRuleName(ruleName);
            evidence.setUserName(userName);
            evidence.setHasWatermark(hasWatermark != null ? hasWatermark : 0);
            evidence.setCreateTime(LocalDateTime.now());
            save(evidence);
            log.info("新增证据成功，applyId={}, photoCount={}", applyId, evidence.getPhotoCount());
            return evidence;
        }
    }

    @Override
    @Transactional
    public ScoreEvidence saveEvidenceSimple(Long applyId, String photoUrls, String location,
                                            Long inspectorId, Long batchId,
                                            String ruleName, String userName) {
        // 规则版本暂不填，或可从规则服务获取，此处留空
        return saveEvidence(applyId, photoUrls, location, inspectorId, batchId, null, ruleName, userName, 1);
    }

    @Override
    @Transactional
    public boolean saveBatch(List<ScoreEvidence> evidenceList) {
        if (evidenceList == null || evidenceList.isEmpty()) {
            return false;
        }
        for (ScoreEvidence evidence : evidenceList) {
            if (evidence.getApplyId() == null || StringUtils.isBlank(evidence.getPhotoUrls())) {
                throw new BusinessException("证据数据不完整");
            }
            evidence.setCreateTime(LocalDateTime.now());
        }
        return saveBatch(evidenceList);
    }

    @Override
    @Transactional
    public boolean deleteByApplyId(Long applyId) {
        if (applyId == null) {
            return false;
        }
        // 逻辑删除（需在实体类中配置 @TableLogic）
        ScoreEvidence evidence = scoreEvidenceMapper.selectByApplyId(applyId);
        if (evidence != null) {
            evidence.setDeleted(1);
            return updateById(evidence);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean forceDeleteByApplyId(Long applyId) {
        if (applyId == null) {
            return false;
        }
        // 物理删除
        return scoreEvidenceMapper.deleteByApplyId(applyId) > 0;
    }

    @Override
    public Long countByBatchId(Long batchId) {
        if (batchId == null) {
            return 0L;
        }
        return scoreEvidenceMapper.countByBatchId(batchId);
    }

    @Override
    public Long countByInspectorId(Long inspectorId) {
        if (inspectorId == null) {
            return 0L;
        }
        return scoreEvidenceMapper.countByInspectorId(inspectorId);
    }

    @Override
    @Transactional
    public boolean updateWatermarkStatus(Long applyId, Integer hasWatermark) {
        if (applyId == null || hasWatermark == null) {
            return false;
        }
        ScoreEvidence evidence = scoreEvidenceMapper.selectByApplyId(applyId);
        if (evidence == null) {
            return false;
        }
        evidence.setHasWatermark(hasWatermark);
        return updateById(evidence);
    }

    @Override
    @Transactional
    public ScoreEvidence appendPhotos(Long applyId, String newPhotoUrls) {
        if (applyId == null || StringUtils.isBlank(newPhotoUrls)) {
            throw new BusinessException("参数不完整");
        }
        ScoreEvidence evidence = scoreEvidenceMapper.selectByApplyId(applyId);
        if (evidence == null) {
            // 如果不存在则新建
            return saveEvidenceSimple(applyId, newPhotoUrls, null, null, null, null, null);
        }
        // 追加
        String oldPhotos = evidence.getPhotoUrls();
        String merged;
        if (StringUtils.isBlank(oldPhotos)) {
            merged = newPhotoUrls;
        } else {
            merged = oldPhotos + "," + newPhotoUrls;
        }
        evidence.setPhotoUrls(merged);
        evidence.setPhotoCount(merged.split(",").length);
        updateById(evidence);
        log.info("追加证据照片成功，applyId={}, 新照片数={}", applyId, newPhotoUrls.split(",").length);
        return evidence;
    }

    @Override
    public boolean existsByApplyId(Long applyId) {
        if (applyId == null) {
            return false;
        }
        return scoreEvidenceMapper.selectByApplyId(applyId) != null;
    }
}