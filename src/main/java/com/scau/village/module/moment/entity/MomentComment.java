package com.scau.village.module.moment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态评论实体类
 * 对应表名：moment_comment
 *
 * @author system
 * @since 2026-07-17
 */
@Data
@TableName("moment_comment")
public class MomentComment {

    /**
     * 评论ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 动态ID
     */
    private Long momentId;

    /**
     * 评论用户ID
     */
    private Integer userId;

    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 租户ID
     */
    private Integer tenantId;
}