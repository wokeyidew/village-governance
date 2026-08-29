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
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数类型从 Long 改为 String，与实体类字段类型保持一致
 * - 包括：applyId、batchId、inspectorId
 * - deleteByApplyId 方法新增，用于申诉撤销评分时联动删除证据
 *
 * @author system
 * @since 2026-08-18
 */
public interface ScoreEvidenceService extends IService<ScoreEvidence> {

    /**
     * 根据积分申请/评分记录ID查询证据
     *
     * @param applyId 积分申请/评分记录ID（雪花ID字符串）
     * @return 证据对象，不存在则返回 null
     */
    ScoreEvidence getByApplyId(String applyId);

    /**
     * 根据积分申请/评分记录ID查询证据，并校验租户权限
     *
     * @param applyId  积分申请/评分记录ID（雪花ID字符串）
     * @param tenantId 租户ID
     * @return 证据对象，不存在或租户不匹配则返回 null
     */
    ScoreEvidence getByApplyIdAndTenant(String applyId, Integer tenantId);

    /**
     * 批量查询证据（根据多个 applyId）
     *
     * @param applyIds 积分申请/评分记录ID列表（雪花ID字符串列表）
     * @return 证据列表，若无匹配则返回空列表
     */
    List<ScoreEvidence> listByApplyIds(List<String> applyIds);

    /**
     * 根据检查批次ID查询所有证据列表
     *
     * @param batchId 检查批次ID（雪花ID字符串）
     * @return 证据列表，若无证据则返回空列表
     */
    List<ScoreEvidence> listByBatchId(String batchId);

    /**
     * 根据检查人ID查询证据列表
     *
     * @param inspectorId 检查人ID（雪花ID字符串）
     * @return 证据列表，若无证据则返回空列表
     */
    List<ScoreEvidence> listByInspectorId(String inspectorId);

    /**
     * 保存证据
     *
     * @param applyId      积分申请/评分记录ID（雪花ID字符串）
     * @param photoUrls    照片URL列表（逗号分隔的字符串）
     * @param location     拍摄位置
     * @param inspectorId  检查人ID（雪花ID字符串）
     * @param batchId      检查批次ID（雪花ID字符串）
     * @param ruleVersion  规则版本号
     * @param ruleName     扣分规则名称（冗余存储）
     * @param userName     户主姓名（冗余存储）
     * @param hasWatermark 是否已添加水印
     * @return 保存后的证据对象
     */
    ScoreEvidence saveEvidence(String applyId, String photoUrls, String location,
                               String inspectorId, String batchId, String ruleVersion,
                               String ruleName, String userName, Integer hasWatermark);

    /**
     * 保存证据（简化版本，自动获取规则版本等）
     *
     * @param applyId     积分申请/评分记录ID（雪花ID字符串）
     * @param photoUrls   照片URL列表（逗号分隔的字符串）
     * @param location    拍摄位置
     * @param inspectorId 检查人ID（雪花ID字符串）
     * @param batchId     检查批次ID（雪花ID字符串）
     * @param ruleName    扣分规则名称（冗余存储）
     * @param userName    户主姓名（冗余存储）
     * @return 保存后的证据对象
     */
    ScoreEvidence saveEvidenceSimple(String applyId, String photoUrls, String location,
                                     String inspectorId, String batchId,
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
     * 用于申诉撤销评分时联动删除
     *
     * @param applyId 积分申请/评分记录ID（雪花ID字符串）
     * @return 是否删除成功
     */
    boolean deleteByApplyId(String applyId);

    /**
     * 根据积分申请/评分记录ID物理删除证据（慎用）
     *
     * @param applyId 积分申请/评分记录ID（雪花ID字符串）
     * @return 是否删除成功
     */
    boolean forceDeleteByApplyId(String applyId);

    /**
     * 统计某个批次的证据总数
     *
     * @param batchId 检查批次ID（雪花ID字符串）
     * @return 证据总数
     */
    Long countByBatchId(String batchId);

    /**
     * 统计某个检查人的证据总数
     *
     * @param inspectorId 检查人ID（雪花ID字符串）
     * @return 证据总数
     */
    Long countByInspectorId(String inspectorId);

    /**
     * 更新水印状态
     *
     * @param applyId       积分申请/评分记录ID（雪花ID字符串）
     * @param hasWatermark  是否已添加水印
     * @return 是否更新成功
     */
    boolean updateWatermarkStatus(String applyId, Integer hasWatermark);

    /**
     * 更新证据照片（追加新照片）
     *
     * @param applyId       积分申请/评分记录ID（雪花ID字符串）
     * @param newPhotoUrls  新增的照片URL（逗号分隔）
     * @return 更新后的证据对象
     */
    ScoreEvidence appendPhotos(String applyId, String newPhotoUrls);

    /**
     * 检查证据是否存在
     *
     * @param applyId 积分申请/评分记录ID（雪花ID字符串）
     * @return true-存在，false-不存在
     */
    boolean existsByApplyId(String applyId);
}