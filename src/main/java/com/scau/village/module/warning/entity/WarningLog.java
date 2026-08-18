package com.scau.village.module.warning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("warning_log")
public class WarningLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer tenantId;
    private String warningType;
    private String warningContent;
    private LocalDateTime occurredAt;
    private Integer isResolved;
    private LocalDateTime resolvedAt;
}