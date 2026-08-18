package com.scau.village.module.appeal.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 村民提交申诉请求DTO
 * 用于村民对扣分项提交申诉
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class SubmitAppealDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被申诉的积分申请/评分记录ID（points_apply.id）
     * 必填
     */
    @NotNull(message = "积分记录ID不能为空")
    private Long applyId;

    /**
     * 申诉理由（必填）
     * 村民说明为什么认为该扣分不合理
     */
    @NotBlank(message = "申诉理由不能为空")
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
     * 便于管理员快速定位到对应的检查批次
     */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;
}