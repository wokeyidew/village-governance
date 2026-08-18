package com.scau.village.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 微信手机号授权登录请求体
 * 用于接收前端通过 wx.getPhoneNumber 获取的加密数据
 * 
 * 前端调用流程：
 * 1. wx.login() → 获取 code
 * 2. 用户在登录按钮上点击授权（open-type="getPhoneNumber"）→ 获取 encryptedData 和 iv
 * 3. 将 code + encryptedData + iv 一起传给后端
 *
 * @author system
 * @since 2026-08-11
 */
@Data
public class WechatPhoneLoginDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * wx.login 获取的临时凭证
     * 用于后端换取 openid 和 session_key
     */
    @NotBlank(message = "code 不能为空")
    private String code;

    /**
     * wx.getPhoneNumber 返回的加密数据
     * 包含手机号等敏感信息，需要用 session_key 解密
     */
    @NotBlank(message = "encryptedData 不能为空")
    private String encryptedData;

    /**
     * wx.getPhoneNumber 返回的初始向量
     * 用于 AES 解密
     */
    @NotBlank(message = "iv 不能为空")
    private String iv;
}