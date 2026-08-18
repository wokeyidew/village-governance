package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.OfflineSyncRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;

/**
 * 离线同步记录 Mapper 接口
 * 对应表名：offline_sync_record
 * 用于记录已处理的离线评分事件，实现幂等性，防止重复提交
 *
 * @author system
 * @since 2026-08-19
 */
@Mapper
public interface OfflineSyncRecordMapper extends BaseMapper<OfflineSyncRecord> {

    /**
     * 根据客户端事件ID查询记录
     * 用于幂等校验：检查该事件是否已被处理
     *
     * @param clientEventId 客户端事件ID（前端生成的UUID）
     * @return 离线同步记录，不存在则返回 null
     */
    @Select("SELECT * FROM offline_sync_record WHERE client_event_id = #{clientEventId}")
    OfflineSyncRecord selectByClientEventId(@Param("clientEventId") String clientEventId);

    /**
     * 检查客户端事件ID是否已被处理
     *
     * @param clientEventId 客户端事件ID
     * @return true-已处理，false-未处理
     */
    @Select("SELECT COUNT(*) > 0 FROM offline_sync_record WHERE client_event_id = #{clientEventId}")
    boolean existsByClientEventId(@Param("clientEventId") String clientEventId);

    /**
     * 根据客户端事件ID删除记录（物理删除）
     * 用于清理测试数据或错误数据
     *
     * @param clientEventId 客户端事件ID
     * @return 影响行数
     */
    @Delete("DELETE FROM offline_sync_record WHERE client_event_id = #{clientEventId}")
    int deleteByClientEventId(@Param("clientEventId") String clientEventId);

    /**
     * 删除指定时间之前的记录（用于定期清理）
     *
     * @param beforeTime 截止时间
     * @return 影响行数
     */
    @Delete("DELETE FROM offline_sync_record WHERE create_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计租户下的离线同步记录总数
     *
     * @param tenantId 租户ID
     * @return 记录总数
     */
    @Select("SELECT COUNT(*) FROM offline_sync_record WHERE tenant_id = #{tenantId}")
    Long countByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * 统计指定时间之后的离线同步记录数
     *
     * @param tenantId   租户ID
     * @param afterTime  开始时间
     * @return 记录总数
     */
    @Select("SELECT COUNT(*) FROM offline_sync_record WHERE tenant_id = #{tenantId} AND create_time >= #{afterTime}")
    Long countByTenantIdAfterTime(@Param("tenantId") Integer tenantId,
                                   @Param("afterTime") LocalDateTime afterTime);
}