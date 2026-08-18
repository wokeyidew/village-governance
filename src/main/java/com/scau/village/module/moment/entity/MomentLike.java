package com.scau.village.module.moment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态点赞实体类
 * 对应表名：moment_like
 *
 * @author system
 * @since 2026-07-17
 */
@Data
@TableName("moment_like")
public class MomentLike {

    /**
     * 点赞ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 动态ID
     */
    private Long momentId;

    /**
     * 点赞用户ID
     */
    private Integer userId;

    /**
     * 点赞时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 租户ID
     */
    private Integer tenantId;
}