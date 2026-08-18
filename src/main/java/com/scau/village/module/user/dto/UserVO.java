package com.scau.village.module.user.dto;

import com.scau.village.common.utils.DesensitizationUtils;
import lombok.Data;

/**
 * 用户信息展示VO（包含脱敏处理）
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