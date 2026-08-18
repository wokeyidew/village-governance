package com.scau.village.module.appeal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.appeal.entity.AppealRecord;
import com.scau.village.module.appeal.vo.AppealVO;
import com.scau.village.module.appeal.vo.AppealDetailVO;

import java.util.List;

/**
 * 申诉记录服务接口
 * 对应表名：appeal_record
 * 提供申诉记录的提交、查询、处理等业务功能
 *
 * @author system
 * @since 2026-08-19
 */
public interface AppealRecordService extends IService<AppealRecord> {

    // ==================== 状态常量 ====================

    /**
     * 状态：申诉中
     */
    String STATUS_PENDING = "pending";

    /**
     * 状态：已处理
     */
    String STATUS_RESOLVED = "resolved";

    /**
     * 决定：维持原判
     */
    String DECISION_UPHELD = "upheld";

    /**
     * 决定：修改评分
     */
    String DECISION_MODIFIED = "modified";

    /**
     * 决定：撤销评分
     */
    String DECISION_REVOKED = "revoked";

    // ==================== 村民端方法 ====================

    /**
     * 村民提交申诉
     *
     * @param applyId         被申诉的积分申请/评分记录ID（points_apply.id）
     * @param userId          当前用户ID（申诉人）
     * @param reason          申诉理由
     * @param evidencePhotos  补充证据照片（逗号分隔）
     * @param tenantId        租户ID
     * @param batchId         关联批次ID
     * @return 创建的申诉记录
     */
    AppealRecord submitAppeal(Long applyId, Long userId, String reason,
                               String evidencePhotos, Integer tenantId, Long batchId);

    /**
     * 获取当前用户的申诉列表（村民端）
     *
     * @param userId 当前用户ID
     * @param status 申诉状态（可选，为空则查询所有）
     * @param page   页码
     * @param size   每页数量
     * @return 分页申诉列表
     */
    Page<AppealVO> getMyAppeals(Long userId, String status, Integer page, Integer size);

    /**
     * 获取申诉详情（村民端）
     *
     * @param appealId 申诉记录ID
     * @param userId   当前用户ID（用于权限校验）
     * @return 申诉详情
     */
    AppealDetailVO getAppealDetail(Long appealId, Long userId);

    /**
     * 统计当前用户各状态的申诉数量
     *
     * @param userId 用户ID
     * @return 各状态申诉数量
     */
    List<AppealVO> getMyAppealCounts(Long userId);

    // ==================== 管理员端方法 ====================

    /**
     * 获取所有申诉列表（管理员端）
     *
     * @param tenantId 租户ID
     * @param status   申诉状态（可选）
     * @param page     页码
     * @param size     每页数量
     * @return 分页申诉列表
     */
    Page<AppealVO> getAdminAppeals(Integer tenantId, String status, Integer page, Integer size);

    /**
     * 获取申诉详情（管理员端）
     *
     * @param appealId 申诉记录ID
     * @return 申诉详情
     */
    AppealDetailVO getAdminAppealDetail(Long appealId);

    /**
     * 获取待处理的申诉列表（管理员端）
     *
     * @param tenantId 租户ID
     * @return 待处理申诉列表
     */
    List<AppealRecord> getPendingAppeals(Integer tenantId);

    /**
     * 管理员处理申诉
     *
     * @param appealId        申诉记录ID
     * @param reviewerId      复核人ID（管理员）
     * @param decision        复核决定：upheld-维持原判，modified-修改评分，revoked-撤销评分
     * @param decisionDetail  处理说明
     * @param newPoints       修改后的分值（仅当 decision = modified 时有效）
     * @return 更新后的申诉记录
     */
    AppealRecord handleAppeal(Long appealId, Long reviewerId, String decision,
                               String decisionDetail, Integer newPoints);

    // ==================== 统计方法 ====================

    /**
     * 统计某个用户指定状态的申诉数量
     *
     * @param userId 用户ID
     * @param status 申诉状态
     * @return 申诉数量
     */
    Long countByUserIdAndStatus(Long userId, String status);

    /**
     * 统计某个批次的申诉总数
     *
     * @param batchId 批次ID
     * @return 申诉总数
     */
    Long countByBatchId(Long batchId);

    /**
     * 检查是否已存在针对某条积分记录的申诉
     *
     * @param applyId 积分申请记录ID
     * @return true-已存在，false-不存在
     */
    boolean existsByApplyId(Long applyId);

    /**
     * 检查是否已存在针对某条积分记录的待处理申诉
     *
     * @param applyId 积分申请记录ID
     * @return true-存在待处理申诉，false-不存在
     */
    boolean hasPendingAppeal(Long applyId);
}