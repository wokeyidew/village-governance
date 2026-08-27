package com.scau.village.module.rectification.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 村民提交整改请求DTO
 * 用于村民提交整改后照片和说明
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class SubmitRectificationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 整改任务ID（必填）
     * 前端传递雪花ID字符串，防止JS精度丢失
     */
    @NotBlank(message = "整改任务ID不能为空")
    private String taskId;

    /**
     * 整改后照片（必填）
     * 前端上传多张照片后，将URL用逗号拼接成一个字符串传入
     * 示例：/upload/rectification/20260819_xxx.jpg,/upload/rectification/20260819_yyy.jpg
     */
    @NotBlank(message = "请上传整改后的照片")
    private String afterPhotos;

    /**
     * 整改说明（可选）
     * 村民可填写整改过程的文字说明
     */
    private String submitRemark;
}