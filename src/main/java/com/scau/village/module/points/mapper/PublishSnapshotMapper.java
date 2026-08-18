package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.PublishSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 红黑榜公示快照 Mapper 接口
 * 对应表名：publish_snapshot
 * 提供快照的增删改查、按批次/月份查询等操作
 *
 * @author system
 * @since 2026-08-19
 */
@Mapper
public interface PublishSnapshotMapper extends BaseMapper<PublishSnapshot> {

    /**
     * 根据批次ID查询快照
     *
     * @param batchId 批次ID
     * @return 快照对象
     */
    @Select("SELECT * FROM publish_snapshot WHERE batch_id = #{batchId} AND deleted = 0")
    PublishSnapshot selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据月份查询快照
     *
     * @param month 月份，格式：yyyy-MM
     * @return 快照列表
     */
    @Select("SELECT * FROM publish_snapshot WHERE month = #{month} AND deleted = 0 ORDER BY create_time DESC")
    List<PublishSnapshot> selectByMonth(@Param("month") String month);

    /**
     * 根据租户ID和月份查询快照（支持分页，但这里仅返回列表）
     *
     * @param tenantId 租户ID
     * @param month    月份（可选）
     * @return 快照列表
     */
    @Select("<script>" +
            "SELECT * FROM publish_snapshot WHERE tenant_id = #{tenantId} AND deleted = 0 " +
            "<if test='month != null and month != \"\"'> AND month = #{month} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<PublishSnapshot> selectByTenantAndMonth(@Param("tenantId") Integer tenantId,
                                                  @Param("month") String month);

    /**
     * 查询最新发布的快照（按发布时间降序取第一条）
     *
     * @param tenantId 租户ID
     * @return 最新快照
     */
    @Select("SELECT * FROM publish_snapshot WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY publish_time DESC LIMIT 1")
    PublishSnapshot selectLatest(@Param("tenantId") Integer tenantId);

    /**
     * 查询某个月份之前的所有快照（用于对比）
     *
     * @param tenantId 租户ID
     * @param month    月份，格式：yyyy-MM
     * @return 快照列表
     */
    @Select("SELECT * FROM publish_snapshot WHERE tenant_id = #{tenantId} AND month &lt; #{month} AND deleted = 0 ORDER BY month DESC")
    List<PublishSnapshot> selectBeforeMonth(@Param("tenantId") Integer tenantId,
                                             @Param("month") String month);
}