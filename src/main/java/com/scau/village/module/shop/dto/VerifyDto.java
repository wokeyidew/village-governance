package com.scau.village.module.shop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 核销请求DTO
 * 用于管理员输入核销码进行核销
 *
 * @author system
 * @since 2026-07-18
 */
@Data
public class VerifyDto {

    /**
     * 8位核销码
     */
    @NotBlank(message = "核销码不能为空")
    @Size(min = 8, max = 8, message = "核销码必须为8位")
    private String code;
}