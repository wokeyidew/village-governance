package com.scau.village.module.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户注册数据传输对象
 * @author system
 * @since 2026-07-17
 */
@Data
public class RegisterDto {

    /**
     * 手机号（必填，11位数字）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 密码（必填，至少6位）
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 租户ID（必填，标识所属村庄）
     */
    @NotNull(message = "租户ID不能为空")
    private Integer tenantId;

    /**
     * 真实姓名（必填）
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 头像URL（可选，注册时可上传头像后传入，也可先不传）
     */
    private String avatar;
}