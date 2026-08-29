package com.scau.village.module.appeal.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 管理员处理申诉请求DTO
 * 用于管理员对村民提交的申诉进行复核处理
 *
 * 修复说明（2026-08-30）：
 * - appealId 保持 String 类型，前端传递雪花ID字符串，防止JS精度丢失
 * - decision 增加 @Pattern 校验，限定只能为 upheld / modified / revoked
 * - 增加字段注释说明
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class HandleAppealDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申诉记录ID（必填）
     * 前端传递雪花ID字符串，防止JS精度丢失
     * 对应 appeal_record.id（VARCHAR(64)）
     */
    @NotBlank(message = "申诉ID不能为空")
    private String appealId;

    /**
     * 复核决定（必填）
     * upheld-维持原判，modified-修改评分，revoked-撤销评分
     */
    @NotBlank(message = "复核决定不能为空")
    @Pattern(regexp = "^(upheld|modified|revoked)$", 
             message = "复核决定只能为 upheld（维持原判）、modified（修改评分）或 revoked（撤销评分）")
    private String decision;

    /**
     * 处理说明（可选）
     * 管理员填写复核意见或说明
     */
    private String decisionDetail;

    /**
     * 修改后的分值（仅当 decision = modified 时有效）
     * 例如：将原扣10分改为扣5分，则传入 5（正数表示扣分分值）
     * 注意：这是一个普通整数值，不是雪花ID，保持 Integer 类型
     * 当 decision = revoked 或 upheld 时，此字段会被忽略
     */
    private Integer newPoints;

}