package com.scau.village.module.points.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证据详情返回VO
 * 用于前端展示扣分项绑定的证据信息
 * 包括照片、拍摄时间、检查人、规则依据等
 *
 * @author system
 * @since 2026-08-18
 */
@Data
public class EvidenceVO {

    /**
     * 是否有证据
     * true-有证据（扣分项强制有证据），false-无证据（加分项通常无证据）
     */
    private Boolean hasEvidence;

    /**
     * 照片URL列表
     */
    private String[] photoUrls;

    /**
     * 照片数量
     */
    private Integer photoCount;

    /**
     * 拍摄位置（GPS坐标或地址描述）
     */
    private String location;

    /**
     * 检查人ID（管理员ID）
     */
    private Long inspectorId;

    /**
     * 检查人姓名（冗余字段，便于前端直接展示）
     */
    private String inspectorName;

    /**
     * 关联检查批次ID
     */
    private Long batchId;

    /**
     * 检查批次名称（冗余字段）
     */
    private String batchName;

    /**
     * 规则版本号
     */
    private String ruleVersion;

    /**
     * 是否已添加水印
     */
    private Boolean hasWatermark;

    /**
     * 扣分规则名称（冗余存储）
     */
    private String ruleName;

    /**
     * 户主姓名（冗余存储）
     */
    private String userName;

    /**
     * 证据创建时间（即拍摄时间或上传时间）
     */
    private LocalDateTime createTime;
}