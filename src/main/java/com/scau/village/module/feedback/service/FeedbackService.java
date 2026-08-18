package com.scau.village.module.feedback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.feedback.dto.FeedbackReplyDto;
import com.scau.village.module.feedback.dto.FeedbackSubmitDto;
import com.scau.village.module.feedback.entity.Feedback;
import com.scau.village.module.feedback.vo.FeedbackVO;

/**
 * 意见反馈服务接口
 * 提供村民端和管理端对反馈的完整操作能力
 *
 * @author system
 * @since 2026-07-23
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 提交反馈（村民端）
     * 村民提交一条新的反馈记录，状态默认为 pending
     *
     * @param userId   当前登录用户ID
     * @param tenantId 租户ID
     * @param dto      反馈提交数据
     * @return 创建的反馈实体
     */
    Feedback submit(Integer userId, Integer tenantId, FeedbackSubmitDto dto);

    /**
     * 获取当前用户的反馈列表（村民端）
     * 仅返回当前登录用户自己提交的反馈，按时间倒序
     *
     * @param userId 当前登录用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 分页反馈列表
     */
    Page<FeedbackVO> getMyFeedbackList(Integer userId, Integer page, Integer size);

    /**
     * 获取反馈详情（村民端/管理员端通用）
     * 村民只能查看自己的反馈，管理员可以查看所有反馈
     *
     * @param id     反馈ID
     * @param userId 当前登录用户ID（用于村民权限校验）
     * @param role   当前登录用户角色（用于判断是否为管理员）
     * @return 反馈详情VO
     */
    FeedbackVO getFeedbackDetail(Integer id, Integer userId, String role);

    /**
     * 管理员获取所有反馈列表（管理员端）
     * 支持按状态筛选，按创建时间倒序
     *
     * @param tenantId 租户ID
     * @param status   状态筛选（可选）：pending-待处理，processing-处理中，resolved-已处理，closed-已关闭
     * @param page     页码
     * @param size     每页大小
     * @return 分页反馈列表
     */
    Page<FeedbackVO> getAdminFeedbackList(Integer tenantId, String status, Integer page, Integer size);

    /**
     * 管理员回复反馈（管理员端）
     * 回复内容会记录到 reply 字段，同时更新 status 和 replyTime
     *
     * @param dto          回复数据（包含反馈ID、回复内容、可选状态更新）
     * @param adminUserId  当前管理员用户ID
     */
    void replyFeedback(FeedbackReplyDto dto, Integer adminUserId);

    /**
     * 管理员删除反馈（管理员端）
     * 逻辑删除，将 deleted 标记设为 1
     *
     * @param id           反馈ID
     * @param adminUserId  当前管理员用户ID
     */
    void deleteFeedback(Integer id, Integer adminUserId);
}