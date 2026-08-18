package com.scau.village.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信登录响应 VO
 * 用于返回微信登录结果及用户状态
 *
 * @author system
 * @since 2026-07-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginVO {

    /**
     * 是否已注册
     * true-已注册（返回 token），false-未注册（返回 openid 供后续注册）
     */
    private Boolean registered;

    /**
     * JWT token（仅当 registered=true 时返回）
     */
    private String token;

    /**
     * 微信 openid（仅当 registered=false 时返回，用于后续注册）
     */
    private String openid;
}