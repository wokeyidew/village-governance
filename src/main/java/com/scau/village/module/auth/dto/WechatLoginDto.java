package com.scau.village.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求 DTO
 * 用于接收前端 wx.login 获取的 code
 *
 * @author system
 * @since 2026-07-31
 */
@Data
public class WechatLoginDto {

    /**
     * wx.login 获取的临时 code
     */
    @NotBlank(message = "登录凭证不能为空")
    private String code;
}