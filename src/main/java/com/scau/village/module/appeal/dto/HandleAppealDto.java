package com.scau.village.module.appeal.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 管理员处理申诉请求DTO
 * 用于管理员对村民提交的申诉进行复核处理
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class HandleAppealDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申诉记录ID（必填）
     */
    @NotNull(message = "申诉ID不能为空")
    private Long appealId;

    /**
     * 复核决定（必填）
     * upheld-维持原判，modified-修改评分，revoked-撤销评分
     */
    @NotNull(message = "复核决定不能为空")
    private String decision;

    /**
     * 处理说明（可选）
     * 管理员填写复核意见或说明
     */
    private String decisionDetail;

    /**
     * 修改后的分值（仅当 decision = modified 时有效）
     * 例如：将原扣10分改为扣5分，则传入 5（正数表示扣分分值）
     */
    private Integer newPoints;
}