package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.points.entity.ScoreEvidence;
import com.scau.village.module.points.mapper.ScoreEvidenceMapper;
import com.scau.village.module.points.service.ScoreEvidenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评分证据服务实现类
 * 对应表名：score_evidence
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数类型从 Long 改为 String，与实体类字段类型保持一致
 * - deleteByApplyId 方法使用 applyId 作为 String 查询并删除
 *
 * @author system
 * @since 2026-08-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreEvidenceServiceImpl extends ServiceImpl<ScoreEvidenceMapper, ScoreEvidence>
        implements ScoreEvidenceService {

    @Override
    public ScoreEvidence getByApplyId(String applyId) {
        if (applyId == null) {
            return null;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId);
        return getOne(wrapper);
    }

    @Override
    public ScoreEvidence getByApplyIdAndTenant(String applyId, Integer tenantId) {
        if (applyId == null || tenantId == null) {
            return null;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId)
               .eq(ScoreEvidence::getTenantId, tenantId);
        return getOne(wrapper);
    }

    @Override
    public List<ScoreEvidence> listByApplyIds(List<String> applyIds) {
        if (applyIds == null || applyIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScoreEvidence::getApplyId, applyIds);
        return list(wrapper);
    }

    @Override
    public List<ScoreEvidence> listByBatchId(String batchId) {
        if (batchId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getBatchId, batchId);
        return list(wrapper);
    }

    @Override
    public List<ScoreEvidence> listByInspectorId(String inspectorId) {
        if (inspectorId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getInspectorId, inspectorId);
        return list(wrapper);
    }

    @Override
    @Transactional
    public ScoreEvidence saveEvidence(String applyId, String photoUrls, String location,
                                       String inspectorId, String batchId, String ruleVersion,
                                       String ruleName, String userName, Integer hasWatermark) {
        if (applyId == null) {
            throw new IllegalArgumentException("applyId 不能为空");
        }
        ScoreEvidence evidence = new ScoreEvidence();
        evidence.setApplyId(applyId);
        evidence.setPhotoUrls(photoUrls);
        evidence.setLocation(location);
        evidence.setInspectorId(inspectorId);
        evidence.setBatchId(batchId);
        evidence.setRuleVersion(ruleVersion);
        evidence.setRuleName(ruleName);
        evidence.setUserName(userName);
        evidence.setHasWatermark(hasWatermark != null ? hasWatermark : 0);
        evidence.setPhotoCount(photoUrls != null ? photoUrls.split(",").length : 0);
        evidence.setCreateTime(LocalDateTime.now());
        save(evidence);
        log.info("保存证据成功，applyId={}", applyId);
        return evidence;
    }

    @Override
    @Transactional
    public ScoreEvidence saveEvidenceSimple(String applyId, String photoUrls, String location,
                                             String inspectorId, String batchId,
                                             String ruleName, String userName) {
        return saveEvidence(applyId, photoUrls, location, inspectorId, batchId, "1.0", ruleName, userName, 1);
    }

    @Override
    @Transactional
    public boolean saveBatch(List<ScoreEvidence> evidenceList) {
        if (evidenceList == null || evidenceList.isEmpty()) {
            return false;
        }
        evidenceList.forEach(e -> {
            if (e.getCreateTime() == null) {
                e.setCreateTime(LocalDateTime.now());
            }
            if (e.getPhotoCount() == null && e.getPhotoUrls() != null) {
                e.setPhotoCount(e.getPhotoUrls().split(",").length);
            }
        });
        return saveBatch(evidenceList);
    }

    @Override
    @Transactional
    public boolean deleteByApplyId(String applyId) {
        if (applyId == null) {
            return false;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId);
        boolean removed = remove(wrapper);
        if (removed) {
            log.info("删除证据成功，applyId={}", applyId);
        } else {
            log.warn("未找到证据，applyId={}", applyId);
        }
        return removed;
    }

    @Override
    @Transactional
    public boolean forceDeleteByApplyId(String applyId) {
        // 物理删除（MyBatis-Plus 的 remove 默认是逻辑删除，若要物理删除需使用 baseMapper.delete）
        if (applyId == null) {
            return false;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId);
        int deleted = baseMapper.delete(wrapper);
        log.info("物理删除证据，applyId={}, 删除数量={}", applyId, deleted);
        return deleted > 0;
    }

    @Override
    public Long countByBatchId(String batchId) {
        if (batchId == null) {
            return 0L;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getBatchId, batchId);
        return count(wrapper);
    }

    @Override
    public Long countByInspectorId(String inspectorId) {
        if (inspectorId == null) {
            return 0L;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getInspectorId, inspectorId);
        return count(wrapper);
    }

    @Override
    @Transactional
    public boolean updateWatermarkStatus(String applyId, Integer hasWatermark) {
        if (applyId == null || hasWatermark == null) {
            return false;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId);
        ScoreEvidence evidence = getOne(wrapper);
        if (evidence == null) {
            log.warn("更新水印状态失败，证据不存在，applyId={}", applyId);
            return false;
        }
        evidence.setHasWatermark(hasWatermark);
        return updateById(evidence);
    }

    @Override
    @Transactional
    public ScoreEvidence appendPhotos(String applyId, String newPhotoUrls) {
        if (applyId == null || StringUtils.isBlank(newPhotoUrls)) {
            return null;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId);
        ScoreEvidence evidence = getOne(wrapper);
        if (evidence == null) {
            log.warn("追加照片失败，证据不存在，applyId={}", applyId);
            return null;
        }
        String existing = evidence.getPhotoUrls();
        String combined;
        if (StringUtils.isBlank(existing)) {
            combined = newPhotoUrls;
        } else {
            // 合并并去重（简单去重：将现有和新增分割后合并，再转成逗号分隔）
            List<String> existingList = Arrays.asList(existing.split(","));
            List<String> newList = Arrays.asList(newPhotoUrls.split(","));
            List<String> merged = new ArrayList<>(existingList);
            for (String url : newList) {
                if (!merged.contains(url)) {
                    merged.add(url);
                }
            }
            combined = merged.stream().collect(Collectors.joining(","));
        }
        evidence.setPhotoUrls(combined);
        evidence.setPhotoCount(combined.split(",").length);
        updateById(evidence);
        log.info("追加照片成功，applyId={}, 新总数={}", applyId, evidence.getPhotoCount());
        return evidence;
    }

    @Override
    public boolean existsByApplyId(String applyId) {
        if (applyId == null) {
            return false;
        }
        LambdaQueryWrapper<ScoreEvidence> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreEvidence::getApplyId, applyId);
        return count(wrapper) > 0;
    }
}