package com.scau.village.module.user.dto;

import lombok.Data;

/**
 * 更新个人资料请求DTO
 * @author system
 * @since 2026-07-17
 */
@Data
public class UpdateProfileDto {
    private String realName;      // 真实姓名（可选）
    private String phone;         // 手机号（可选）
    private String avatar;        // 头像URL（可选）
    private String oldPassword;   // 旧密码（修改密码时必填）
    private String newPassword;   // 新密码（修改密码时必填）
    private String idCard;        // 身份证号（可选，更新时校验格式）
}