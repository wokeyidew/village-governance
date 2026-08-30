package com.scau.village.module.rectification.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 整改任务列表展示视图对象
 * 用于村民端和管理端的整改任务列表展示
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 字段类型从 Long 改为 String，与实体类 RectificationTask 保持一致
 * - 包括：id, applyId, userId
 *
 * @author system
 * @since 2026-08-19
 */
@Data
public class RectificationTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 整改任务ID（雪花ID字符串）
     */
    private String id;

    /**
     * 关联积分申请/评分记录ID（雪花ID字符串）
     */
    private String applyId;

    /**
     * 责任户主用户ID（雪花ID字符串）
     */
    private String userId;

    /**
     * 责任户主姓名
     */
    private String userName;

    /**
     * 扣分规则名称
     */
    private String ruleName;

    /**
     * 任务状态
     * pending-待整改，reviewing-待复核，resolved-已销项，overdue-逾期
     */
    private String status;

    /**
     * 整改要求
     */
    private String requirement;

    /**
     * 整改截止时间
     */
    private LocalDateTime deadline;

    /**
     * 整改前照片（缩略图取第一张即可）
     * 逗号分隔的URL列表
     */
    private String beforePhotos;

    /**
     * 整改后照片（逗号分隔）
     */
    private String afterPhotos;

    /**
     * 村民提交整改时间
     */
    private LocalDateTime submitTime;

    /**
     * 复核结果：passed-通过，rejected-不通过
     */
    private String reviewResult;

    /**
     * 整改奖励积分
     */
    private Integer rewardPoints;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否已逾期（前端可据此显示红色警告）
     */
    private Boolean overdue;

    /**
     * 各状态任务数量（仅用于统计接口）
     */
    private Long count;

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
            case "pending":
                return "待整改";
            case "reviewing":
                return "待复核";
            case "resolved":
                return "已销项";
            case "overdue":
                return "已逾期";
            default:
                return status;
        }
    }
}