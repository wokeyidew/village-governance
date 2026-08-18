package com.scau.village.module.moment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 村民动态（朋友圈）实体类
 * 对应表名：moment
 *
 * @author system
 * @since 2026-07-17
 */
@Data
@TableName("moment")
public class Moment {

    /**
     * 动态ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布用户ID
     */
    private Integer userId;

    /**
     * 动态内容（文字）
     */
    private String content;

    /**
     * 图片URL列表，多个用逗号分隔
     */
    private String images;

    /**
     * 点赞数
     */
    private Integer likesCount;

    /**
     * 评论数
     */
    private Integer commentsCount;

    /**
     * 状态：1-正常，0-已删除
     */
    private Integer status;

    /**
     * 创建时间（发布时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 租户ID
     */
    private Integer tenantId;
}