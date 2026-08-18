package com.scau.village.module.rectification.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 整改任务详情视图对象
 * 用于整改任务详情页展示，包含完整的前后对比信息
 * 村民端和管理端共用此VO
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class RectificationDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 整改任务ID
     */
    private Long id;

    /**
     * 关联积分申请/评分记录ID
     */
    private Long applyId;

    /**
     * 责任户主用户ID
     */
    private Long userId;

    /**
     * 责任户主姓名
     */
    private String userName;

    /**
     * 责任户主手机号
     */
    private String userPhone;

    /**
     * 关联检查批次ID
     */
    private Long batchId;

    /**
     * 扣分规则名称
     */
    private String ruleName;

    /**
     * 整改要求
     */
    private String requirement;

    /**
     * 整改截止时间
     */
    private LocalDateTime deadline;

    /**
     * 任务状态
     * pending-待整改，reviewing-待复核，resolved-已销项，overdue-逾期
     */
    private String status;

    /**
     * 整改前照片（原始扣分证据照片）
     * 逗号分隔的URL列表
     */
    private String beforePhotos;

    /**
     * 整改后照片（村民提交）
     * 逗号分隔的URL列表
     */
    private String afterPhotos;

    /**
     * 村民提交整改时间
     */
    private LocalDateTime submitTime;

    /**
     * 村民整改说明
     */
    private String submitRemark;

    /**
     * 复核结果：passed-通过，rejected-不通过
     */
    private String reviewResult;

    /**
     * 复核备注（管理员填写）
     */
    private String reviewRemark;

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
     * 整改奖励积分（扣分值的50%）
     */
    private Integer rewardPoints;

    /**
     * 检查人ID
     */
    private Long inspectorId;

    /**
     * 检查人姓名
     */
    private String inspectorName;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否已逾期
     */
    private Boolean overdue;

    // ==================== 状态常量（便于前端使用） ====================

    /**
     * 状态：待整改
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 状态：待复核
     */
    public static final String STATUS_REVIEWING = "reviewing";

    /**
     * 状态：已销项
     */
    public static final String STATUS_RESOLVED = "resolved";

    /**
     * 状态：逾期
     */
    public static final String STATUS_OVERDUE = "overdue";

    /**
     * 复核结果：通过
     */
    public static final String REVIEW_PASSED = "passed";

    /**
     * 复核结果：不通过
     */
    public static final String REVIEW_REJECTED = "rejected";

    // ==================== 便捷方法 ====================

    /**
     * 获取状态的中文描述
     */
    public String getStatusText() {
        if (status == null) {
            return "";
        }
        switch (status) {
            case STATUS_PENDING:
                return "待整改";
            case STATUS_REVIEWING:
                return "待复核";
            case STATUS_RESOLVED:
                return "已销项";
            case STATUS_OVERDUE:
                return "已逾期";
            default:
                return status;
        }
    }

    /**
     * 获取复核结果的中文描述
     */
    public String getReviewResultText() {
        if (reviewResult == null) {
            return "";
        }
        switch (reviewResult) {
            case REVIEW_PASSED:
                return "复核通过";
            case REVIEW_REJECTED:
                return "复核不通过";
            default:
                return reviewResult;
        }
    }

    /**
     * 判断是否可提交整改（待整改或待复核状态）
     */
    public boolean canSubmit() {
        return STATUS_PENDING.equals(status) || STATUS_REVIEWING.equals(status);
    }

    /**
     * 判断是否可复核（待复核状态）
     */
    public boolean canReview() {
        return STATUS_REVIEWING.equals(status);
    }

    /**
     * 判断是否已销项
     */
    public boolean isResolved() {
        return STATUS_RESOLVED.equals(status);
    }

    /**
     * 判断是否有整改后照片
     */
    public boolean hasAfterPhotos() {
        return afterPhotos != null && !afterPhotos.isEmpty();
    }

    /**
     * 获取整改前照片数组
     */
    public String[] getBeforePhotoArray() {
        if (beforePhotos == null || beforePhotos.isEmpty()) {
            return new String[0];
        }
        return beforePhotos.split(",");
    }

    /**
     * 获取整改后照片数组
     */
    public String[] getAfterPhotoArray() {
        if (afterPhotos == null || afterPhotos.isEmpty()) {
            return new String[0];
        }
        return afterPhotos.split(",");
    }
}