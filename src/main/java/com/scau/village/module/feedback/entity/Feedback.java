package com.scau.village.module.feedback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 意见反馈实体类
 * 对应表名：feedback
 *
 * @author system
 * @since 2026-07-23
 */
@Data
@TableName("feedback")
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 提交用户ID
     */
    private Integer userId;

    /**
     * 租户ID
     */
    private Integer tenantId;

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
     * 联系方式（可选）
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
     * 回复人ID（管理员）
     */
    private Integer repliedBy;

    /**
     * 创建时间（提交时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}