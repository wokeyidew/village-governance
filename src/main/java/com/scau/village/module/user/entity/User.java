package com.scau.village.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user
 *
 * @author system
 * @since 2026-07-17
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户ID（所属村庄）
     */
    private Integer tenantId;

    /**
     * 微信openid（用于微信登录，可选）
     */
    private String openid;

    /**
     * 手机号（登录账号）
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 村小组
     */
    private String villageGroup;

    /**
     * 是否党员（0-否，1-是）
     */
    private Integer isPartyMember;

    /**
     * 角色：VILLAGER, GRID_MEMBER, VILLAGE_ADMIN, TOWN_ADMIN, SUPER_ADMIN
     */
    private String role;

    /**
     * 当前积分余额
     */
    private Integer points;

    /**
     * 登录密码（BCrypt加密存储）
     */
    private String password;

    /**
     * 头像URL（新增字段，支持注册/修改时上传）
     */
    private String avatar;

    /**
     * 关联居民档案ID（注册时绑定）
     */
    private Integer residentProfileId;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}