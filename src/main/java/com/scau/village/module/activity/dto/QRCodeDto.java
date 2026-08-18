package com.scau.village.module.activity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 生成活动二维码请求DTO
 *
 * @author system
 * @since 2026-07-19
 */
@Data
public class QRCodeDto {

    /**
     * 活动ID（必填）
     */
    @NotNull(message = "活动ID不能为空")
    private Integer activityId;

    /**
     * 二维码类型（必填）
     * signin-签到，checkout-签退
     */
    @NotBlank(message = "二维码类型不能为空")
    private String type;

    /**
     * 是否动态码（默认true）
     * true-动态码（10秒刷新），false-固定码
     */
    private Boolean dynamic = true;
}