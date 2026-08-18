package com.scau.village.module.shop.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待核销列表返回VO（管理员端）
 * 用于展示所有待核销的兑换记录
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class PendingVerifyVO {

    /**
     * 兑换记录ID
     */
    private Integer id;

    /**
     * 核销码
     */
    private String exchangeCode;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 兑换时间（线上兑换时间）
     */
    private LocalDateTime exchangedAt;

    /**
     * 状态：pending-待核销
     */
    private String status;
}