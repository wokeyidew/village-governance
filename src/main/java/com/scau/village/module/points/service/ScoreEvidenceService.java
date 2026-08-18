package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.ScoreEvidence;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评分证据服务接口
 * 对应表名：score_evidence
 * 提供证据的增删改查、批量操作等业务功能
 *
 * @author system
 * @since 2026-08-18
 */
public interface ScoreEvidenceService extends IService<ScoreEvidence> {

    /**
     * 根据积分申请/评分记录ID查询证据
     *
     * @param applyId 积分申请/评分记录ID（points_apply.id）
     * @return 证据对象，不存在则返回 null
     */
    ScoreEvidence getByApplyId(Long applyId);

    /**
     * 根据积分申请/评分记录ID查询证据，并校验租户权限
     *
     * @param applyId  积分申请/评分记录ID
     * @param tenantId 租户ID
     * @return 证据对象，不存在或租户不匹配则返回 null
     */
    ScoreEvidence getByApplyIdAndTenant(Long applyId, Integer tenantId);

    /**
     * 批量查询证据（根据多个 applyId）
     *
     * @param applyIds 积分申请/评分记录ID列表
     * @return 证据列表，若无匹配则返回空列表
     */
    List<ScoreEvidence> listByApplyIds(List<Long> applyIds);

    /**
     * 根据检查批次ID查询所有证据列表
     *
     * @param batchId 检查批次ID（inspection_batch.id）
     * @return 证据列表，若无证据则返回空列表
     */
    List<ScoreEvidence> listByBatchId(Long batchId);

    /**
     * 根据检查人ID查询证据列表
     *
     * @param inspectorId 检查人ID（管理员ID）
     * @return 证据列表，若无证据则返回空列表
     */
    List<ScoreEvidence> listByInspectorId(Long inspectorId);

    /**
     * 保存证据
     *
     * @param applyId      积分申请/评分记录ID
     * @param photoUrls    照片URL列表（逗号分隔的字符串）
     * @param location     拍摄位置
     * @param inspectorId  检查人ID
     * @param batchId      检查批次ID
     * @param ruleVersion  规则版本号
     * @param ruleName     扣分规则名称（冗余存储）
     * @param userName     户主姓名（冗余存储）
     * @param hasWatermark 是否已添加水印
     * @return 保存后的证据对象
     */
    ScoreEvidence saveEvidence(Long applyId, String photoUrls, String location,
                               Long inspectorId, Long batchId, String ruleVersion,
                               String ruleName, String userName, Integer hasWatermark);

    /**
     * 保存证据（简化版本，自动获取规则版本等）
     *
     * @param applyId     积分申请/评分记录ID
     * @param photoUrls   照片URL列表（逗号分隔的字符串）
     * @param location    拍摄位置
     * @param inspectorId 检查人ID
     * @param batchId     检查批次ID
     * @param ruleName    扣分规则名称（冗余存储）
     * @param userName    户主姓名（冗余存储）
     * @return 保存后的证据对象
     */
    ScoreEvidence saveEvidenceSimple(Long applyId, String photoUrls, String location,
                                     Long inspectorId, Long batchId,
                                     String ruleName, String userName);

    /**
     * 批量保存证据
     *
     * @param evidenceList 证据对象列表
     * @return 是否保存成功
     */
    boolean saveBatch(List<ScoreEvidence> evidenceList);

    /**
     * 根据积分申请/评分记录ID逻辑删除证据
     *
     * @param applyId 积分申请/评分记录ID
     * @return 是否删除成功
     */
    boolean deleteByApplyId(Long applyId);

    /**
     * 根据积分申请/评分记录ID物理删除证据（慎用）
     *
     * @param applyId 积分申请/评分记录ID
     * @return 是否删除成功
     */
    boolean forceDeleteByApplyId(Long applyId);

    /**
     * 统计某个批次的证据总数
     *
     * @param batchId 检查批次ID
     * @return 证据总数
     */
    Long countByBatchId(Long batchId);

    /**
     * 统计某个检查人的证据总数
     *
     * @param inspectorId 检查人ID
     * @return 证据总数
     */
    Long countByInspectorId(Long inspectorId);

    /**
     * 更新水印状态
     *
     * @param applyId        积分申请/评分记录ID
     * @param hasWatermark   是否已添加水印
     * @return 是否更新成功
     */
    boolean updateWatermarkStatus(Long applyId, Integer hasWatermark);

    /**
     * 更新证据照片（追加新照片）
     *
     * @param applyId     积分申请/评分记录ID
     * @param newPhotoUrls 新增的照片URL（逗号分隔）
     * @return 更新后的证据对象
     */
    ScoreEvidence appendPhotos(Long applyId, String newPhotoUrls);

    /**
     * 检查证据是否存在
     *
     * @param applyId 积分申请/评分记录ID
     * @return true-存在，false-不存在
     */
    boolean existsByApplyId(Long applyId);
}