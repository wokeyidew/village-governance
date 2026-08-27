package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.ImportantContribution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 重要贡献认定 Mapper 接口
 * 对应表：important_contribution
 * 提供重要贡献的增删改查及统计功能
 *
 * @author system
 * @since 2026-08-28
 */
@Mapper
public interface ImportantContributionMapper extends BaseMapper<ImportantContribution> {

    /**
     * 查询用户的所有重要贡献记录（按创建时间降序）
     *
     * @param userId 用户ID
     * @return 重要贡献记录列表
     */
    @Select("SELECT * FROM important_contribution " +
            "WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<ImportantContribution> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户已通过的重要贡献记录（仅 approved）
     *
     * @param userId 用户ID
     * @return 已通过的重要贡献记录列表
     */
    @Select("SELECT * FROM important_contribution " +
            "WHERE user_id = #{userId} " +
            "AND status = 'approved' " +
            "ORDER BY create_time DESC")
    List<ImportantContribution> selectApprovedByUserId(@Param("userId") Integer userId);

    /**
     * 查询租户下所有待认定的重要贡献（状态为 pending）
     * 用于管理员审核列表
     *
     * @param tenantId 租户ID
     * @return 待认定记录列表
     */
    @Select("SELECT * FROM important_contribution " +
            "WHERE tenant_id = #{tenantId} " +
            "AND status = 'pending' " +
            "ORDER BY create_time ASC")
    List<ImportantContribution> selectPendingList(@Param("tenantId") Integer tenantId);

    /**
     * 查询租户下所有已通过的重要贡献（状态为 approved）
     * 用于公示或统计
     *
     * @param tenantId 租户ID
     * @return 已通过记录列表
     */
    @Select("SELECT * FROM important_contribution " +
            "WHERE tenant_id = #{tenantId} " +
            "AND status = 'approved' " +
            "ORDER BY create_time DESC")
    List<ImportantContribution> selectApprovedList(@Param("tenantId") Integer tenantId);

    /**
     * 更新认定为通过
     *
     * @param id           记录ID
     * @param approvedBy   认定人ID
     * @param approvedTime 认定时间
     * @return 更新行数
     */
    @Update("UPDATE important_contribution " +
            "SET status = 'approved', " +
            "approved_by = #{approvedBy}, " +
            "approved_time = #{approvedTime} " +
            "WHERE id = #{id}")
    int approve(@Param("id") Long id,
                @Param("approvedBy") Integer approvedBy,
                @Param("approvedTime") java.time.LocalDateTime approvedTime);

    /**
     * 更新认定为驳回
     *
     * @param id    记录ID
     * @param remark 驳回原因（可选）
     * @return 更新行数
     */
    @Update("UPDATE important_contribution " +
            "SET status = 'rejected', " +
            "remark = #{remark} " +
            "WHERE id = #{id}")
    int reject(@Param("id") Long id,
               @Param("remark") String remark);

    /**
     * 统计用户的贡献总积分（仅统计已通过记录）
     *
     * @param userId 用户ID
     * @return 总积分
     */
    @Select("SELECT COALESCE(SUM(points), 0) FROM important_contribution " +
            "WHERE user_id = #{userId} " +
            "AND status = 'approved'")
    Integer sumPointsByUserId(@Param("userId") Integer userId);

    /**
     * 按贡献类型统计数量（用于驾驶舱图表）
     *
     * @param tenantId 租户ID
     * @param status   状态筛选（可选，传入 null 则统计全部）
     * @return 贡献类型统计列表，每个 Map 包含 contributionType 和 count
     */
    @Select("<script>" +
            "SELECT contribution_type as contributionType, COUNT(*) as count " +
            "FROM important_contribution " +
            "WHERE tenant_id = #{tenantId} " +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "GROUP BY contribution_type " +
            "ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> countByType(@Param("tenantId") Integer tenantId,
                                          @Param("status") String status);

    /**
     * 统计租户下各状态的数量
     *
     * @param tenantId 租户ID
     * @return 状态统计列表，每个 Map 包含 status 和 count
     */
    @Select("SELECT status, COUNT(*) as count " +
            "FROM important_contribution " +
            "WHERE tenant_id = #{tenantId} " +
            "GROUP BY status")
    List<Map<String, Object>> countByStatus(@Param("tenantId") Integer tenantId);

    /**
     * 查询指定季度内的重要贡献记录
     * 用于季度结算时统计季度重要贡献积分
     *
     * @param tenantId    租户ID
     * @param userId      用户ID
     * @param startTime   季度开始时间
     * @param endTime     季度结束时间
     * @return 重要贡献记录列表
     */
    @Select("SELECT * FROM important_contribution " +
            "WHERE tenant_id = #{tenantId} " +
            "AND user_id = #{userId} " +
            "AND status = 'approved' " +
            "AND create_time >= #{startTime} " +
            "AND create_time < #{endTime}")
    List<ImportantContribution> selectByUserAndDateRange(@Param("tenantId") Integer tenantId,
                                                         @Param("userId") Integer userId,
                                                         @Param("startTime") java.time.LocalDateTime startTime,
                                                         @Param("endTime") java.time.LocalDateTime endTime);

}