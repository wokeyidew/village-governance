package com.scau.village.module.resident.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.resident.entity.ResidentProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 居民档案 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author system
 * @since 2026-07-31
 */
@Mapper
public interface ResidentProfileMapper extends BaseMapper<ResidentProfile> {

    /**
     * 根据关键词搜索户主（用于注册时选择）
     * 支持按户主姓名、手机号、地址模糊搜索
     *
     * @param tenantId 租户ID
     * @param keyword  搜索关键词
     * @return 匹配的居民档案列表
     */
    @Select("SELECT * FROM resident_profile WHERE tenant_id = #{tenantId} " +
            "AND deleted = 0 " +
            "AND (owner_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR address LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY owner_name ASC LIMIT 20")
    List<ResidentProfile> searchByKeyword(@Param("tenantId") Integer tenantId,
                                          @Param("keyword") String keyword);

    /**
     * 根据手机号和户主姓名精确匹配（用于注册验证）
     *
     * @param tenantId  租户ID
     * @param phone     手机号
     * @param ownerName 户主姓名
     * @return 匹配的居民档案，不存在返回 null
     */
    @Select("SELECT * FROM resident_profile WHERE tenant_id = #{tenantId} " +
            "AND deleted = 0 " +
            "AND phone = #{phone} " +
            "AND owner_name = #{ownerName}")
    ResidentProfile matchByPhoneAndOwner(@Param("tenantId") Integer tenantId,
                                         @Param("phone") String phone,
                                         @Param("ownerName") String ownerName);
}