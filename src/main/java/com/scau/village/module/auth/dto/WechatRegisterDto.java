package com.scau.village.module.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 微信注册请求 DTO
 * 用于村民首次注册时完善个人信息
 *
 * @author system
 * @since 2026-07-31
 */
@Data
public class WechatRegisterDto {

    /**
     * 微信 openid（从第一步登录获取）
     */
    @NotBlank(message = "openid不能为空")
    private String openid;

    /**
     * 手机号（通过微信 getPhoneNumber 授权获取）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "姓名不能超过20个字符")
    private String realName;

    /**
     * 选择的户主姓名（从搜索列表中选择）
     */
    @NotBlank(message = "请选择户主")
    private String ownerName;

    /**
     * 所属区域（可选，辅助匹配）
     */
    private String villageGroup;
}