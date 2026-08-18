package com.scau.village.module.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动二维码实体类
 * 对应表名：activity_qrcode
 * 用于存储活动签到/签退的二维码信息（固定码和动态码）
 *
 * @author system
 * @since 2026-07-19
 */
@Data
@TableName("activity_qrcode")
public class ActivityQRCode {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 活动ID
     */
    private Integer activityId;

    /**
     * 二维码类型：signin-签到，checkout-签退
     */
    private String qrcodeType;

    /**
     * 动态码唯一标识（用于动态码验证，动态码时非空）
     */
    private String qrcodeKey;

    /**
     * 固定码（活动期间不变，用于固定二维码）
     */
    private String fixedCode;

    /**
     * 状态：1-有效，0-失效
     */
    private Integer status;

    /**
     * 动态码过期时间（动态码时有效）
     */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}