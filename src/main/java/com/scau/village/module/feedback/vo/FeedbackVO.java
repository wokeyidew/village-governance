package com.scau.village.module.feedback.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 意见反馈展示VO（返回给前端）
 * 包含反馈信息及关联的用户/管理员信息
 *
 * @author system
 * @since 2026-07-23
 */
@Data
public class FeedbackVO {

    /**
     * 反馈ID
     */
    private Integer id;

    /**
     * 提交用户ID
     */
    private Integer userId;

    /**
     * 提交用户姓名
     */
    private String userName;

    /**
     * 提交用户头像URL
     */
    private String userAvatar;

    /**
     * 反馈类型：complaint-投诉，suggestion-建议，question-咨询，other-其他
     */
    private String category;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 图片URL列表（逗号分隔）
     */
    private String images;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 处理状态：pending-待处理，processing-处理中，resolved-已处理，closed-已关闭
     */
    private String status;

    /**
     * 管理员回复内容
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 回复人姓名
     */
    private String replyByName;

    /**
     * 提交时间
     */
    private LocalDateTime createTime;
}