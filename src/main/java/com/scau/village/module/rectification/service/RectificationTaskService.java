package com.scau.village.module.rectification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.rectification.entity.RectificationTask;
import com.scau.village.module.rectification.vo.RectificationTaskVO;
import com.scau.village.module.rectification.vo.RectificationDetailVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 整改任务服务接口
 * 对应表名：rectification_task
 * 提供整改任务的创建、查询、提交、复核等业务功能
 *
 * @author system
 * @since 2026-08-19
 */
public interface RectificationTaskService extends IService<RectificationTask> {

    // ==================== 状态常量 ====================

    /**
     * 状态：待整改
     */
    String STATUS_PENDING = "pending";

    /**
     * 状态：待复核（村民已提交整改）
     */
    String STATUS_REVIEWING = "reviewing";

    /**
     * 状态：已销项（复核通过）
     */
    String STATUS_RESOLVED = "resolved";

    /**
     * 状态：逾期（超过截止时间未整改）
     */
    String STATUS_OVERDUE = "overdue";

    /**
     * 复核结果：通过
     */
    String REVIEW_PASSED = "passed";

    /**
     * 复核结果：不通过
     */
    String REVIEW_REJECTED = "rejected";

    // ==================== 创建方法 ====================

    /**
     * 创建整改任务（扣分后自动调用）
     *
     * @param applyId       积分申请/评分记录ID
     * @param userId        责任户主用户ID
     * @param batchId       检查批次ID
     * @param ruleName      扣分规则名称
     * @param requirement   整改要求
     * @param deadline      整改截止时间
     * @param beforePhotos  整改前照片（扣分证据照片）
     * @param inspectorId   检查人ID
     * @return 创建的整改任务对象
     */
    RectificationTask createTask(Long applyId, Long userId, Long batchId, String ruleName,
                                  String requirement, LocalDateTime deadline,
                                  String beforePhotos, Long inspectorId);

    // ==================== 村民端查询方法 ====================

    /**
     * 获取当前用户的整改任务列表（村民端）
     *
     * @param userId 当前用户ID
     * @param status 任务状态（可选，为空则查询所有）
     * @param page   页码
     * @param size   每页数量
     * @return 分页整改任务列表
     */
    Page<RectificationTaskVO> getMyTasks(Long userId, String status, Integer page, Integer size);

    /**
     * 获取当前用户各状态的任务数量统计（村民端）
     *
     * @param userId 当前用户ID
     * @return 各状态任务数量
     */
    List<RectificationTaskVO> getMyTaskCounts(Long userId);

    /**
     * 获取整改任务详情（村民端）
     * 包含整改前后对比照片
     *
     * @param taskId 整改任务ID
     * @param userId 当前用户ID（用于权限校验）
     * @return 整改任务详情
     */
    RectificationDetailVO getTaskDetail(Long taskId, Long userId);

    // ==================== 村民端操作方法 ====================

    /**
     * 村民提交整改
     *
     * @param taskId       整改任务ID
     * @param userId       当前用户ID（用于权限校验）
     * @param afterPhotos  整改后照片（逗号分隔）
     * @param submitRemark 整改说明（可选）
     * @return 更新后的整改任务
     */
    RectificationTask submitRectification(Long taskId, Long userId, String afterPhotos, String submitRemark);

    // ==================== 管理员端查询方法 ====================

    /**
     * 获取所有整改任务列表（管理员端）
     *
     * @param tenantId 租户ID
     * @param status   任务状态（可选，为空则查询所有）
     * @param page     页码
     * @param size     每页数量
     * @return 分页整改任务列表
     */
    Page<RectificationTaskVO> getAdminTaskList(Integer tenantId, String status, Integer page, Integer size);

    /**
     * 获取整改任务详情（管理员端）
     * 包含完整的整改前后对比信息
     *
     * @param taskId 整改任务ID
     * @return 整改任务详情
     */
    RectificationDetailVO getAdminTaskDetail(Long taskId);

    /**
     * 根据批次ID获取整改任务列表（管理员端）
     *
     * @param batchId 批次ID
     * @param status  任务状态（可选）
     * @return 整改任务列表
     */
    List<RectificationTask> getTasksByBatchId(Long batchId, String status);

    // ==================== 管理员端操作方法 ====================

    /**
     * 管理员复核整改任务
     *
     * @param taskId       整改任务ID
     * @param reviewerId   复核人ID（管理员）
     * @param reviewResult 复核结果：passed-通过，rejected-不通过
     * @param reviewRemark 复核备注（可选）
     * @return 更新后的整改任务
     */
    RectificationTask reviewTask(Long taskId, Long reviewerId, String reviewResult, String reviewRemark);

    // ==================== 统计方法 ====================

    /**
     * 统计某个用户指定状态的任务数量
     *
     * @param userId 用户ID
     * @param status 任务状态
     * @return 任务数量
     */
    Long countByUserIdAndStatus(Long userId, String status);

    /**
     * 统计某个批次的整改任务总数
     *
     * @param batchId 批次ID
     * @return 任务总数
     */
    Long countByBatchId(Long batchId);

    /**
     * 计算某个批次的整改完成率
     *
     * @param batchId 批次ID
     * @return 完成率（0-100之间的整数）
     */
    Integer calculateCompletionRate(Long batchId);

    /**
     * 统计当前用户待整改任务数量（用于角标提示）
     *
     * @param userId 用户ID
     * @return 待整改任务数量
     */
    Long countPendingTasks(Long userId);

    // ==================== 定时任务方法 ====================

    /**
     * 批量将逾期任务状态更新为 overdue
     * 由定时任务调用
     *
     * @return 更新的任务数量
     */
    int batchUpdateOverdueStatus();

    /**
     * 根据积分申请记录ID更新整改状态
     * 用于申诉撤销评分时联动更新
     *
     * @param applyId 积分申请记录ID
     * @param status  新状态
     * @return 更新行数
     */
    int updateStatusByApplyId(Long applyId, String status);
}