package com.scau.village.module.activity.vo;

import lombok.Data;

/**
 * 活动二维码响应VO
 * 用于返回给前端展示的二维码信息
 *
 * @author system
 * @since 2026-07-19
 */
@Data
public class QRCodeVO {

    /**
     * 二维码图片（Base64格式，可直接用于image标签的src）
     * 例如：data:image/png;base64,iVBORw0KGgoAAAANS...
     */
    private String qrcodeImage;

    /**
     * 固定码（活动期间不变，用于固定二维码）
     */
    private String fixedCode;

    /**
     * 动态码key（用于动态码验证，前端无需关心）
     */
    private String dynamicKey;

    /**
     * 动态码有效期（秒），固定码时为0
     */
    private Integer expireSeconds;

    /**
     * 二维码类型：signin-签到，checkout-签退
     */
    private String qrcodeType;

    /**
     * 是否动态码
     */
    private Boolean isDynamic;
}