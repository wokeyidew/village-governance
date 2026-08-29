package com.scau.village.module.appeal.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 村民提交申诉请求DTO
 * 用于村民对扣分项提交申诉
 *
 * 修复说明（2026-08-30）：
 * - applyId 和 batchId 保持 String 类型，前端传递雪花ID字符串，防止JS精度丢失
 * - reason 增加 @Size 校验，限制申诉理由长度
 * - 完善字段注释，明确对应数据库表和字段用途
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class SubmitAppealDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被申诉的积分申请/评分记录ID（必填）
     * 对应 points_apply.id（VARCHAR(64)，雪花ID）
     * 前端传递雪花ID字符串，防止JS精度丢失
     */
    @NotBlank(message = "积分记录ID不能为空")
    private String applyId;

    /**
     * 申诉理由（必填）
     * 村民说明为什么认为该扣分不合理
     * 最大长度500字符
     */
    @NotBlank(message = "申诉理由不能为空")
    @Size(max = 500, message = "申诉理由不能超过500字符")
    private String reason;

    /**
     * 补充证据照片（可选）
     * 村民可上传1-3张补充照片作为证据
     * 多张照片URL用逗号拼接
     * 示例：/upload/appeal/20260819_xxx.jpg,/upload/appeal/20260819_yyy.jpg
     */
    private String evidencePhotos;

    /**
     * 关联检查批次ID（必填）
     * 对应 inspection_batch.id（VARCHAR(64)，雪花ID）
     * 前端传递雪花ID字符串，防止JS精度丢失
     * 便于管理员快速定位到对应的检查批次
     */
    @NotBlank(message = "批次ID不能为空")
    private String batchId;

}