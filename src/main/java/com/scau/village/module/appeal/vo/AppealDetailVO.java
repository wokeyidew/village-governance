package com.scau.village.module.appeal.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申诉详情视图对象
 * 用于展示申诉的完整信息，包括关联的积分记录和规则信息
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class AppealDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 申诉基本信息 ====================

    /**
     * 申诉记录ID
     */
    private Long id;

    /**
     * 关联积分申请/评分记录ID（points_apply.id）
     */
    private Long applyId;

    /**
     * 申诉人用户ID
     */
    private Long userId;

    /**
     * 申诉人姓名
     */
    private String userName;

    /**
     * 申诉理由
     */
    private String reason;

    /**
     * 补充证据照片（逗号分隔）
     */
    private String evidencePhotos;

    /**
     * 申诉状态：pending-申诉中，resolved-已处理
     */
    private String status;

    /**
     * 复核决定：upheld-维持原判，modified-修改评分，revoked-撤销评分
     */
    private String decision;

    /**
     * 处理说明
     */
    private String decisionDetail;

    /**
     * 复核人ID（管理员）
     */
    private Long reviewerId;

    /**
     * 复核人姓名
     */
    private String reviewerName;

    /**
     * 复核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 关联检查批次ID
     */
    private Long batchId;

    /**
     * 申诉提交时间
     */
    private LocalDateTime createTime;

    // ==================== 关联积分记录信息 ====================

    /**
     * 积分记录状态（points_apply.status）
     * 如：approved、pending、rejected
     */
    private String applyStatus;

    /**
     * 积分记录描述
     */
    private String applyDescription;

    /**
     * 积分记录图片
     */
    private String applyImages;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则分值（正数加分，负数扣分）
     */
    private Integer rulePoints;

    // ==================== 便捷方法 ====================

    /**
     * 获取状态的中文描述
     */
    public String getStatusText() {
        if (status == null) return "";
        switch (status) {
            case "pending":
                return "申诉中";
            case "resolved":
                return "已处理";
            default:
                return status;
        }
    }

    /**
     * 获取决定的中文描述
     */
    public String getDecisionText() {
        if (decision == null) return "";
        switch (decision) {
            case "upheld":
                return "维持原判";
            case "modified":
                return "修改评分";
            case "revoked":
                return "撤销评分";
            default:
                return decision;
        }
    }

    /**
     * 判断是否申诉中
     */
    public boolean isPending() {
        return "pending".equals(status);
    }

    /**
     * 判断是否已处理
     */
    public boolean isResolved() {
        return "resolved".equals(status);
    }

    /**
     * 判断是否撤销评分
     */
    public boolean isRevoked() {
        return "revoked".equals(decision);
    }

    /**
     * 判断是否修改评分
     */
    public boolean isModified() {
        return "modified".equals(decision);
    }

    /**
     * 判断是否维持原判
     */
    public boolean isUpheld() {
        return "upheld".equals(decision);
    }

    /**
     * 获取证据照片数组
     */
    public String[] getEvidencePhotoArray() {
        if (evidencePhotos == null || evidencePhotos.isEmpty()) {
            return new String[0];
        }
        return evidencePhotos.split(",");
    }
}