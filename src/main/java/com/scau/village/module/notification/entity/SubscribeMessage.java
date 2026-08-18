package com.scau.village.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信订阅消息实体类
 * 用于记录管理员订阅的微信模板消息
 * 对应表名：subscribe_message
 *
 * @author system
 * @since 2026-07-18
 */
@Data
@TableName("subscribe_message")
public class SubscribeMessage {

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 管理员用户ID
     */
    private Integer userId;

    /**
     * 微信模板ID（订阅消息模板）
     */
    private String templateId;

    /**
     * 管理员微信openid
     */
    private String openid;

    /**
     * 订阅状态：1-已订阅，0-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}