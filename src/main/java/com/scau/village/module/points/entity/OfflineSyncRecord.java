package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 离线同步记录实体类
 * 对应表名：offline_sync_record
 * 用于记录已处理的离线评分事件，实现幂等性，防止重复提交
 *
 * @author system
 * @since 2026-08-19
 */
@Data
@TableName("offline_sync_record")
public class OfflineSyncRecord {

    /**
     * 主键ID（雪花算法）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户端事件ID（前端生成的UUID）
     * 唯一索引，用于幂等校验
     */
    @TableField("client_event_id")
    private String clientEventId;

    /**
     * 是否已处理：1-已处理，0-未处理（实际上线后插入即已处理）
     */
    @TableField("processed")
    private Integer processed;

    /**
     * 处理时间（即服务器处理该事件的时间）
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Integer tenantId;

    /**
     * 创建时间（记录插入时间）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}