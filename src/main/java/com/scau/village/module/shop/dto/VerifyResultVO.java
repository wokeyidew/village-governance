package com.scau.village.module.shop.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 核销结果返回VO
 * 用于核销成功后返回详细信息给前端
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class VerifyResultVO {

    /**
     * 核销码
     */
    private String exchangeCode;

    /**
     * 核销状态：success-成功，fail-失败
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 兑换用户姓名
     */
    private String userName;

    /**
     * 线上兑换时间
     */
    private LocalDateTime exchangedAt;

    /**
     * 线下核销时间
     */
    private LocalDateTime verifiedAt;

    /**
     * 核销方式：scan-扫码，manual-手动输入
     */
    private String verifyMethod;

    /**
     * 核销人ID（管理员ID）
     */
    private Integer verifiedBy;

    /**
     * 核销人姓名（管理员姓名）
     */
    private String verifiedByName;
}