package com.scau.village.module.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 重要贡献认定实体类
 * 对应表：important_contribution
 * 用于记录村民重要贡献的认定过程，包含五步认定法中的核心数据
 *
 * 贡献类型说明：
 * - rescue: 抢险救灾（参与救火、抗洪、救人等）
 * - report: 举报重大隐患（发现并报告危房、火灾隐患等）
 * - mediate: 协助化解重大矛盾（调解邻里纠纷、促成和解等）
 * - resource: 争取外部资源（招商引资、争取政府项目等）
 * - help: 长期帮扶困难家庭（持续帮扶孤寡老人、困难户等）
 *
 * @author system
 * @since 2026-08-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("important_contribution")
public class ImportantContribution {

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Integer tenantId;

    /**
     * 贡献人用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 贡献类型编码
     * rescue: 抢险救灾
     * report: 举报重大隐患
     * mediate: 协助化解重大矛盾
     * resource: 争取外部资源
     * help: 长期帮扶困难家庭
     */
    @TableField("contribution_type")
    private String contributionType;

    /**
     * 贡献描述（详细说明贡献事迹）
     */
    @TableField("contribution_desc")
    private String contributionDesc;

    /**
     * 认定积分值
     */
    @TableField("points")
    private Integer points;

    /**
     * 认定状态
     * pending: 待认定（已提交，等待核实）
     * approved: 已通过（集体评议+公示无异议）
     * rejected: 已驳回（经核实不属实或不符合认定标准）
     */
    @TableField("status")
    private String status;

    /**
     * 认定人ID（村委成员）
     */
    @TableField("approved_by")
    private Integer approvedBy;

    /**
     * 认定时间（集体评议通过并公示无异议后）
     */
    @TableField("approved_time")
    private LocalDateTime approvedTime;

    /**
     * 佐证照片URL列表（逗号分隔）
     */
    @TableField("evidence_photos")
    private String evidencePhotos;

    /**
     * 备注（认定过程中的补充说明）
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间（发起认定时间）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 状态常量（便于代码中引用） ====================

    /**
     * 状态：待认定
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 状态：已通过
     */
    public static final String STATUS_APPROVED = "approved";

    /**
     * 状态：已驳回
     */
    public static final String STATUS_REJECTED = "rejected";

    // ==================== 贡献类型常量 ====================

    /**
     * 贡献类型：抢险救灾
     */
    public static final String TYPE_RESCUE = "rescue";

    /**
     * 贡献类型：举报重大隐患
     */
    public static final String TYPE_REPORT = "report";

    /**
     * 贡献类型：协助化解重大矛盾
     */
    public static final String TYPE_MEDIATE = "mediate";

    /**
     * 贡献类型：争取外部资源
     */
    public static final String TYPE_RESOURCE = "resource";

    /**
     * 贡献类型：长期帮扶困难家庭
     */
    public static final String TYPE_HELP = "help";

}