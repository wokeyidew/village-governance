package com.scau.village.module.resident.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.resident.entity.ResidentProfile;

import java.util.List;

/**
 * 居民档案服务接口
 * 提供居民档案的查询、匹配等功能
 *
 * @author system
 * @since 2026-07-31
 */
public interface ResidentProfileService extends IService<ResidentProfile> {

    /**
     * 根据关键词搜索户主（用于注册时选择）
     * 支持按户主姓名、手机号、地址模糊搜索
     *
     * @param tenantId 租户ID
     * @param keyword  搜索关键词
     * @return 匹配的居民档案列表（最多20条）
     */
    List<ResidentProfile> searchByKeyword(Integer tenantId, String keyword);

    /**
     * 根据手机号和户主姓名精确匹配（用于注册验证）
     *
     * @param tenantId  租户ID
     * @param phone     手机号
     * @param ownerName 户主姓名
     * @return 匹配的居民档案，不存在返回 null
     */
    ResidentProfile matchByPhoneAndOwner(Integer tenantId, String phone, String ownerName);
}