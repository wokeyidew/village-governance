package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.ActivityParticipation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 活动参与记录 Mapper 接口
 * 对应表：activity_participation
 * 提供活动参与记录的增删改查及统计功能
 *
 * @author system
 * @since 2026-08-28
 */
@Mapper
public interface ActivityParticipationMapper extends BaseMapper<ActivityParticipation> {

    /**
     * 查询用户的所有活动参与记录（按参与时间降序）
     *
     * @param userId 用户ID
     * @return 活动参与记录列表
     */
    @Select("SELECT * FROM activity_participation " +
            "WHERE user_id = #{userId} " +
            "ORDER BY participate_time DESC")
    List<ActivityParticipation> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户在指定时间段内的活动参与记录
     * 用于季度结算时统计 activity_count
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间（不含）
     * @return 活动参与记录列表
     */
    @Select("SELECT * FROM activity_participation " +
            "WHERE user_id = #{userId} " +
            "AND participate_time >= #{startTime} " +
            "AND participate_time < #{endTime} " +
            "ORDER BY participate_time DESC")
    List<ActivityParticipation> selectByUserIdAndDateRange(@Param("userId") Integer userId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户在指定时间段内的活动参与次数
     * 用于季度结算时填充 activity_count 字段
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间（不含）
     * @return 参与次数
     */
    @Select("SELECT COUNT(*) FROM activity_participation " +
            "WHERE user_id = #{userId} " +
            "AND participate_time >= #{startTime} " +
            "AND participate_time < #{endTime}")
    Integer countByUserIdAndDateRange(@Param("userId") Integer userId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户在指定时间段内获得的活动积分总和
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间（不含）
     * @return 积分总和
     */
    @Select("SELECT COALESCE(SUM(earned_points), 0) FROM activity_participation " +
            "WHERE user_id = #{userId} " +
            "AND participate_time >= #{startTime} " +
            "AND participate_time < #{endTime}")
    Integer sumPointsByUserIdAndDateRange(@Param("userId") Integer userId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定活动的参与记录
     *
     * @param activityId 活动ID
     * @return 活动参与记录列表
     */
    @Select("SELECT * FROM activity_participation " +
            "WHERE activity_id = #{activityId} " +
            "ORDER BY participate_time DESC")
    List<ActivityParticipation> selectByActivityId(@Param("activityId") Integer activityId);

    /**
     * 统计指定活动的参与人数（去重）
     *
     * @param activityId 活动ID
     * @return 参与人数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM activity_participation " +
            "WHERE activity_id = #{activityId}")
    Integer countParticipantsByActivityId(@Param("activityId") Integer activityId);

    /**
     * 按活动类型统计参与次数（用于驾驶舱图表）
     *
     * @param tenantId   租户ID
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @return 活动类型统计列表，每个 Map 包含 activityType 和 count
     */
    @Select("<script>" +
            "SELECT activity_type as activityType, COUNT(*) as count " +
            "FROM activity_participation " +
            "WHERE tenant_id = #{tenantId} " +
            "<if test='startTime != null'>" +
            "AND participate_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND participate_time < #{endTime} " +
            "</if>" +
            "GROUP BY activity_type " +
            "ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> countByActivityType(@Param("tenantId") Integer tenantId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 按月统计活动参与趋势（用于驾驶舱图表）
     *
     * @param tenantId  租户ID
     * @param year      年份
     * @return 月度统计列表，每个 Map 包含 month 和 count
     */
    @Select("SELECT DATE_FORMAT(participate_time, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM activity_participation " +
            "WHERE tenant_id = #{tenantId} " +
            "AND YEAR(participate_time) = #{year} " +
            "GROUP BY DATE_FORMAT(participate_time, '%Y-%m') " +
            "ORDER BY month ASC")
    List<Map<String, Object>> countByMonth(@Param("tenantId") Integer tenantId,
                                           @Param("year") Integer year);

    /**
     * 统计租户下所有用户在指定时间段内的活动参与总次数
     *
     * @param tenantId   租户ID
     * @param startTime  开始时间
     * @param endTime    结束时间（不含）
     * @return 总参与次数
     */
    @Select("SELECT COUNT(*) FROM activity_participation " +
            "WHERE tenant_id = #{tenantId} " +
            "AND participate_time >= #{startTime} " +
            "AND participate_time < #{endTime}")
    Integer countTotalByDateRange(@Param("tenantId") Integer tenantId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计租户下所有用户在指定时间段内的活动获得总积分
     *
     * @param tenantId   租户ID
     * @param startTime  开始时间
     * @param endTime    结束时间（不含）
     * @return 总积分
     */
    @Select("SELECT COALESCE(SUM(earned_points), 0) FROM activity_participation " +
            "WHERE tenant_id = #{tenantId} " +
            "AND participate_time >= #{startTime} " +
            "AND participate_time < #{endTime}")
    Integer sumTotalPointsByDateRange(@Param("tenantId") Integer tenantId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定活动类型下，参与次数最多的前N名用户
     * 用于红榜排名中的"活动参与次数"维度统计
     *
     * @param tenantId   租户ID
     * @param startTime  开始时间
     * @param endTime    结束时间（不含）
     * @param limit      限制条数
     * @return 用户排名列表，每个 Map 包含 userId 和 count
     */
    @Select("SELECT user_id as userId, COUNT(*) as count " +
            "FROM activity_participation " +
            "WHERE tenant_id = #{tenantId} " +
            "AND participate_time >= #{startTime} " +
            "AND participate_time < #{endTime} " +
            "GROUP BY user_id " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopUsersByActivityCount(@Param("tenantId") Integer tenantId,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("limit") Integer limit);

}