package com.scau.village.module.resident.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 居民档案实体类
 * 对应表名：resident_profile
 * 用于存储从 Excel 导入的村民档案数据，供注册时匹配验证
 *
 * @author system
 * @since 2026-07-31
 */
@Data
@TableName("resident_profile")
public class ResidentProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户ID（所属村庄）
     */
    private Integer tenantId;

    /**
     * 户主姓名
     */
    private String ownerName;

    /**
     * 家庭成员姓名（逗号分隔，可选）
     */
    private String familyMembers;

    /**
     * 联系方式（户主或家庭电话）
     */
    private String phone;

    /**
     * 身份证号（可选）
     */
    private String idCard;

    /**
     * 所属区域（一区/二区/三区等）
     */
    private String villageGroup;

    /**
     * 门牌号/地号
     */
    private String address;

    /**
     * 户编号（可选）
     */
    private String householdCode;

    /**
     * 人数
     */
    private Integer totalPeople;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}