package com.scau.village.module.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.notification.entity.SubscribeMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 微信订阅消息 Mapper 接口
 * 对应表名：subscribe_message
 *
 * @author system
 * @since 2026-07-18
 */
@Mapper
public interface SubscribeMessageMapper extends BaseMapper<SubscribeMessage> {

    /**
     * 根据用户ID和模板ID查询订阅记录
     *
     * @param userId     用户ID
     * @param templateId 模板ID
     * @return 订阅记录，不存在返回null
     */
    @Select("SELECT * FROM subscribe_message WHERE user_id = #{userId} AND template_id = #{templateId}")
    SubscribeMessage selectByUserIdAndTemplateId(@Param("userId") Integer userId,
                                                  @Param("templateId") String templateId);

    /**
     * 根据用户ID查询所有订阅记录
     *
     * @param userId 用户ID
     * @return 订阅记录列表
     */
    @Select("SELECT * FROM subscribe_message WHERE user_id = #{userId} AND status = 1 ORDER BY create_time DESC")
    List<SubscribeMessage> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据租户ID查询所有订阅记录（用于批量通知）
     *
     * @param tenantId 租户ID
     * @return 订阅记录列表
     */
    @Select("SELECT * FROM subscribe_message WHERE tenant_id = #{tenantId} AND status = 1 ORDER BY create_time DESC")
    List<SubscribeMessage> selectByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * 根据模板ID查询所有订阅记录（用于批量通知）
     *
     * @param templateId 模板ID
     * @return 订阅记录列表
     */
    @Select("SELECT * FROM subscribe_message WHERE template_id = #{templateId} AND status = 1 ORDER BY create_time DESC")
    List<SubscribeMessage> selectByTemplateId(@Param("templateId") String templateId);
}