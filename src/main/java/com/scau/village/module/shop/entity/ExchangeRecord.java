package com.scau.village.module.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录实体类
 * 对应表名：exchange_record
 *
 * @author system
 * @since 2026-07-18
 */
@Data
@TableName("exchange_record")
public class ExchangeRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer tenantId;

    private Integer userId;

    private Integer productId;

    /**
     * 8位核销码（唯一）
     */
    private String exchangeCode;

    /**
     * 状态：pending-待核销，used-已核销，expired-已过期（不再使用）
     */
    private String status;

    /**
     * 线下核销时间（即 usedTime，核销时设置）
     */
    private LocalDateTime usedTime;

    /**
     * 核销码过期时间（已废弃，不再使用，保留字段）
     */
    private LocalDateTime expireTime;

    /**
     * 线上兑换时间（即 createTime，记录生成时即为兑换时间）
     */
    private LocalDateTime createTime;

    /**
     * 核销人ID（管理员ID），核销时记录
     */
    private Integer verifiedBy;

    /**
     * 核销方式：scan-扫码，manual-手动输入
     */
    private String verifyMethod;
}