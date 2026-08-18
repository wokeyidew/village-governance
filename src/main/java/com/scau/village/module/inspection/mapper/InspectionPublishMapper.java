package com.scau.village.module.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.inspection.entity.InspectionPublish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评比发布记录 Mapper 接口
 * 对应表名：inspection_publish
 *
 * @author system
 * @since 2026-07-18
 */
@Mapper
public interface InspectionPublishMapper extends BaseMapper<InspectionPublish> {

    /**
     * 根据批次ID查询发布记录
     *
     * @param batchId 检查批次ID
     * @return 发布记录，不存在返回null
     */
    @Select("SELECT * FROM inspection_publish WHERE batch_id = #{batchId} AND deleted = 0")
    InspectionPublish selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据批次ID查询已发布的记录
     *
     * @param batchId 检查批次ID
     * @return 发布记录，不存在返回null
     */
    @Select("SELECT * FROM inspection_publish WHERE batch_id = #{batchId} AND status = 'published' AND deleted = 0")
    InspectionPublish selectPublishedByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据租户ID查询所有已发布的记录
     *
     * @param tenantId 租户ID
     * @return 已发布记录列表
     */
    @Select("SELECT * FROM inspection_publish WHERE tenant_id = #{tenantId} AND status = 'published' AND deleted = 0 ORDER BY published_at DESC")
    List<InspectionPublish> selectPublishedByTenantId(@Param("tenantId") Integer tenantId);

    /**
     * 根据状态查询发布记录
     *
     * @param status 状态：draft-草稿，published-已发布
     * @param tenantId 租户ID
     * @return 发布记录列表
     */
    @Select("SELECT * FROM inspection_publish WHERE tenant_id = #{tenantId} AND status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<InspectionPublish> selectByStatus(@Param("tenantId") Integer tenantId,
                                           @Param("status") String status);

    /**
     * 查询租户下最新的已发布记录（用于首页展示）
     *
     * @param tenantId 租户ID
     * @return 最新已发布记录
     */
    @Select("SELECT * FROM inspection_publish WHERE tenant_id = #{tenantId} AND status = 'published' AND deleted = 0 ORDER BY published_at DESC LIMIT 1")
    InspectionPublish selectLatestPublished(@Param("tenantId") Integer tenantId);
}