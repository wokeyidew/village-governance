package com.scau.village.module.user.dto;

import com.scau.village.common.utils.DesensitizationUtils;
import lombok.Data;

/**
 * 用户信息展示VO（包含脱敏处理）
 * 
 * 积分体系说明（v2.0）：
 * - totalEarnedPoints（总获得积分）：历史累计获得的积分总和，只增不减，兑换时不扣减
 * - availablePoints（可用积分）：当前可使用的积分余额，获得时增加，兑换时扣减
 * - points（当前积分余额）：保留用于兼容，建议前端优先使用 availablePoints
 *
 * @author system
 * @since 2026-07-17
 */
@Data
public class UserVO {
    private Integer id;
    private String phone;
    private String realName;
    private String idCard;
    private Integer points;
    private String avatar;      // 头像URL
    private String role;        // 用户角色（VILLAGER / VILLAGE_ADMIN / GRID_MEMBER / TOWN_ADMIN / SUPER_ADMIN）
    private Integer residentProfileId; // 关联居民档案ID

    // ==================== v2.0 新增字段 ====================

    /**
     * 总获得积分（永久累加，只增不减）
     * 用于：荣誉总榜排名、年度评优、历史荣誉展示
     * 兑换商品时不扣减此字段
     */
    private Integer totalEarnedPoints;

    /**
     * 当前可用积分（兑换时扣减）
     * 用于：积分兑换商品、参与活动消耗
     */
    private Integer availablePoints;

    /**
     * 手机号脱敏（中间4位替换为****）
     */
    public String getPhone() {
        return DesensitizationUtils.mobile(phone);
    }

    /**
     * 身份证号脱敏（保留前6位和后4位，中间替换为********）
     */
    public String getIdCard() {
        return DesensitizationUtils.idCard(idCard);
    }

    // 以下为手动实现的 setter（Lombok @Data 会自动生成，但若重写了 getter，建议也保留 setter）
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    // avatar、role、residentProfileId 使用默认 getter/setter，无需脱敏
}