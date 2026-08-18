package com.scau.village.module.inspection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.inspection.entity.InspectionPublish;
import com.scau.village.module.inspection.vo.InspectionResultVO;

import java.util.List;

/**
 * 评比发布服务接口
 * 用于管理检查批次评比结果的发布与查看
 *
 * @author system
 * @since 2026-07-18
 */
public interface InspectionPublishService extends IService<InspectionPublish> {

    /**
     * 发布评比结果
     * 根据批次ID生成快照，存入 inspection_publish 表，状态设为 published
     *
     * @param batchId    检查批次ID
     * @param tenantId   租户ID
     * @param publisherId 发布人ID（管理员）
     * @param remark     发布备注（可选）
     * @return 发布记录
     */
    InspectionPublish publish(Long batchId, Integer tenantId, Integer publisherId, String remark);

    /**
     * 取消发布（将状态改回 draft）
     *
     * @param batchId 检查批次ID
     * @param tenantId 租户ID（用于权限校验）
     * @return true-取消成功，false-取消失败
     */
    boolean unpublish(Long batchId, Integer tenantId);

    /**
     * 检查指定批次是否已发布
     *
     * @param batchId 检查批次ID
     * @return true-已发布，false-未发布或不存在
     */
    boolean isPublished(Long batchId);

    /**
     * 根据批次ID获取发布记录（含快照）
     *
     * @param batchId 检查批次ID
     * @return 发布记录，不存在返回null
     */
    InspectionPublish getPublishByBatchId(Long batchId);

    /**
     * 获取已发布的评比结果（供村民端查看）
     * 返回当前用户在指定批次中的得分明细
     *
     * @param batchId 检查批次ID
     * @param userId  当前用户ID
     * @return 评比结果VO（仅当已发布时返回，否则返回null）
     */
    InspectionResultVO getResultForUser(Long batchId, Integer userId);

    /**
     * 根据租户ID获取所有已发布的批次列表（供村民端查看有哪些批次已发布）
     *
     * @param tenantId 租户ID
     * @return 已发布的批次列表（仅包含批次基本信息）
     */
    List<InspectionPublish> getPublishedBatchList(Integer tenantId);

    /**
     * 管理员获取某个批次的完整发布快照（含所有用户数据）
     *
     * @param batchId  检查批次ID
     * @param tenantId 租户ID（用于权限校验）
     * @return 发布记录（含完整快照）
     */
    InspectionPublish getFullPublishDetail(Long batchId, Integer tenantId);
}