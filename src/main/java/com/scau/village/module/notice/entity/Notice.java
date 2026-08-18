package com.scau.village.module.notice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 村务通知实体类
 * 对应表名：notice
 *
 * @author system
 * @since 2026-07-17
 */
@Data
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer tenantId;

    private String title;

    private String content;

    private String images;

    private Integer isTop;

    private Integer readCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}