package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.QuarterlySnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 季度快照 Mapper 接口
 * 对应表：quarterly_snapshot
 * 提供红榜、蜕变榜、帮扶榜的查询功能
 *
 * 纯注解方式（无 XML），避免 SAXParseException
 *
 * 修复说明（v2.0.9）：
 * - 所有查询增加 LEFT JOIN user 表，返回 userName 字段
 * - 使用表别名 qs，避免字段冲突
 *
 * @author system
 * @since 2026-08-28
 */
@Mapper
public interface QuarterlySnapshotMapper extends BaseMapper<QuarterlySnapshot> {

    /**
     * 查询指定季度的红榜列表
     * 红榜标准：tag = 'red'，按 rank_points 升序排列（排名数字越小越靠前）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识，如：2026-Q3
     * @param limit    限制条数（默认取前20名）
     * @return 红榜列表
     */
    @Select("SELECT qs.*, u.real_name AS userName " +
            "FROM quarterly_snapshot qs " +
            "LEFT JOIN user u ON qs.user_id = u.id " +
            "WHERE qs.tenant_id = #{tenantId} " +
            "AND qs.quarter = #{quarter} " +
            "AND qs.tag = 'red' " +
            "ORDER BY qs.rank_points ASC " +
            "LIMIT #{limit}")
    List<QuarterlySnapshot> selectRedList(@Param("tenantId") Integer tenantId,
                                          @Param("quarter") String quarter,
                                          @Param("limit") Integer limit);

    /**
     * 查询指定季度的蜕变榜列表
     * 蜕变榜标准：tag = 'progress'，按 rank_progress 升序排列（进步最大排最前）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识，如：2026-Q3
     * @param limit    限制条数（默认取前20名）
     * @return 蜕变榜列表
     */
    @Select("SELECT qs.*, u.real_name AS userName " +
            "FROM quarterly_snapshot qs " +
            "LEFT JOIN user u ON qs.user_id = u.id " +
            "WHERE qs.tenant_id = #{tenantId} " +
            "AND qs.quarter = #{quarter} " +
            "AND qs.tag = 'progress' " +
            "ORDER BY qs.rank_progress ASC " +
            "LIMIT #{limit}")
    List<QuarterlySnapshot> selectProgressList(@Param("tenantId") Integer tenantId,
                                               @Param("quarter") String quarter,
                                               @Param("limit") Integer limit);

    /**
     * 查询指定季度的帮扶榜列表（管理员后台）
     * 帮扶榜标准：tag = 'warning'，即本季净积分为负的家庭
     * 按 quarter_net_points 升序排列（积分最低的最需要帮扶）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识，如：2026-Q3
     * @return 帮扶榜列表
     */
    @Select("SELECT qs.*, u.real_name AS userName " +
            "FROM quarterly_snapshot qs " +
            "LEFT JOIN user u ON qs.user_id = u.id " +
            "WHERE qs.tenant_id = #{tenantId} " +
            "AND qs.quarter = #{quarter} " +
            "AND qs.tag = 'warning' " +
            "ORDER BY qs.quarter_net_points ASC")
    List<QuarterlySnapshot> selectWarningList(@Param("tenantId") Integer tenantId,
                                              @Param("quarter") String quarter);

    /**
     * 查询指定季度单个用户的快照数据
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param userId   用户ID
     * @return 季度快照数据，不存在则返回 null
     */
    @Select("SELECT qs.*, u.real_name AS userName " +
            "FROM quarterly_snapshot qs " +
            "LEFT JOIN user u ON qs.user_id = u.id " +
            "WHERE qs.tenant_id = #{tenantId} " +
            "AND qs.quarter = #{quarter} " +
            "AND qs.user_id = #{userId}")
    QuarterlySnapshot selectUserQuarterData(@Param("tenantId") Integer tenantId,
                                            @Param("quarter") String quarter,
                                            @Param("userId") Integer userId);

    /**
     * 查询指定季度所有用户的快照数据（按积分排名升序）
     * 用于季度结算时计算排名
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 该季度所有用户快照列表
     */
    @Select("SELECT qs.*, u.real_name AS userName " +
            "FROM quarterly_snapshot qs " +
            "LEFT JOIN user u ON qs.user_id = u.id " +
            "WHERE qs.tenant_id = #{tenantId} " +
            "AND qs.quarter = #{quarter} " +
            "ORDER BY qs.quarter_earned_points DESC, " +
            "qs.rule_count DESC, " +
            "qs.activity_count DESC, " +
            "qs.no_penalty_days DESC, " +
            "qs.last_activity_time ASC")
    List<QuarterlySnapshot> selectAllByQuarter(@Param("tenantId") Integer tenantId,
                                               @Param("quarter") String quarter);

    /**
     * 查询指定季度进步分排名（用于蜕变榜排名计算）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 按进步分降序排列的快照列表
     */
    @Select("<script>" +
            "SELECT qs.*, u.real_name AS userName " +
            "FROM quarterly_snapshot qs " +
            "LEFT JOIN user u ON qs.user_id = u.id " +
            "WHERE qs.tenant_id = #{tenantId} " +
            "AND qs.quarter = #{quarter} " +
            "AND qs.progress_points &gt; 0 " +
            "ORDER BY qs.progress_points DESC" +
            "</script>")
    List<QuarterlySnapshot> selectAllByProgress(@Param("tenantId") Integer tenantId,
                                                @Param("quarter") String quarter);

    /**
     * 批量更新标签（用于季度结算时打标）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param userId   用户ID
     * @param tag      标签值（red/progress/normal/warning）
     * @return 更新行数
     */
    @Update("UPDATE quarterly_snapshot " +
            "SET tag = #{tag} " +
            "WHERE tenant_id = #{tenantId} " +
            "AND quarter = #{quarter} " +
            "AND user_id = #{userId}")
    int updateTag(@Param("tenantId") Integer tenantId,
                  @Param("quarter") String quarter,
                  @Param("userId") Integer userId,
                  @Param("tag") String tag);

    /**
     * 获取某季度红榜总人数（用于统计）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 红榜人数
     */
    @Select("SELECT COUNT(*) FROM quarterly_snapshot " +
            "WHERE tenant_id = #{tenantId} " +
            "AND quarter = #{quarter} " +
            "AND tag = 'red'")
    Integer countRedList(@Param("tenantId") Integer tenantId,
                         @Param("quarter") String quarter);

    /**
     * 获取某季度蜕变榜总人数（用于统计）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 蜕变榜人数
     */
    @Select("SELECT COUNT(*) FROM quarterly_snapshot " +
            "WHERE tenant_id = #{tenantId} " +
            "AND quarter = #{quarter} " +
            "AND tag = 'progress'")
    Integer countProgressList(@Param("tenantId") Integer tenantId,
                              @Param("quarter") String quarter);

    /**
     * 获取某季度帮扶榜总人数（用于统计）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 帮扶榜人数
     */
    @Select("SELECT COUNT(*) FROM quarterly_snapshot " +
            "WHERE tenant_id = #{tenantId} " +
            "AND quarter = #{quarter} " +
            "AND tag = 'warning'")
    Integer countWarningList(@Param("tenantId") Integer tenantId,
                             @Param("quarter") String quarter);

}