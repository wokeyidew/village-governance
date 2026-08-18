package com.scau.village.module.appeal.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申诉列表展示视图对象
 * 用于村民端和管理端的申诉列表展示
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class AppealVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申诉记录ID
     */
    private Long id;

    /**
     * 关联积分申请/评分记录ID
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
     * 补充证据照片（缩略图取第一张）
     */
    private String evidencePhotos;

    /**
     * 申诉状态
     * pending-申诉中，resolved-已处理
     */
    private String status;

    /**
     * 复核决定
     * upheld-维持原判，modified-修改评分，revoked-撤销评分
     */
    private String decision;

    /**
     * 处理说明
     */
    private String decisionDetail;

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

    /**
     * 各状态申诉数量（仅用于统计接口）
     */
    private Long count;

    // ==================== 状态常量 ====================

    /**
     * 状态：申诉中
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 状态：已处理
     */
    public static final String STATUS_RESOLVED = "resolved";

    /**
     * 决定：维持原判
     */
    public static final String DECISION_UPHELD = "upheld";

    /**
     * 决定：修改评分
     */
    public static final String DECISION_MODIFIED = "modified";

    /**
     * 决定：撤销评分
     */
    public static final String DECISION_REVOKED = "revoked";

    // ==================== 便捷方法 ====================

    /**
     * 获取状态的中文描述
     *
     * @return 状态中文描述
     */
    public String getStatusText() {
        if (status == null) {
            return "";
        }
        switch (status) {
            case STATUS_PENDING:
                return "申诉中";
            case STATUS_RESOLVED:
                return "已处理";
            default:
                return status;
        }
    }

    /**
     * 获取决定的中文描述
     *
     * @return 决定中文描述
     */
    public String getDecisionText() {
        if (decision == null) {
            return "";
        }
        switch (decision) {
            case DECISION_UPHELD:
                return "维持原判";
            case DECISION_MODIFIED:
                return "修改评分";
            case DECISION_REVOKED:
                return "撤销评分";
            default:
                return decision;
        }
    }

    /**
     * 判断是否申诉中
     *
     * @return true-申诉中，false-已处理
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * 判断是否已处理
     *
     * @return true-已处理，false-申诉中
     */
    public boolean isResolved() {
        return STATUS_RESOLVED.equals(status);
    }

    /**
     * 获取证据照片数组
     *
     * @return 证据照片URL数组
     */
    public String[] getEvidencePhotoArray() {
        if (evidencePhotos == null || evidencePhotos.isEmpty()) {
            return new String[0];
        }
        return evidencePhotos.split(",");
    }
}