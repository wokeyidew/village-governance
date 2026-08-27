package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.QuarterlySnapshot;

import java.util.List;

/**
 * 季度快照 Service 接口
 * 对应表：quarterly_snapshot
 * 提供季度结算、榜单查询、个人数据查询等功能
 *
 * 核心功能说明：
 * 1. 季度结算：每季度末自动计算所有村民的季度积分数据，生成排名和标签
 * 2. 红榜查询：获取本季度表现优秀的村民（前10%）
 * 3. 蜕变榜查询：获取本季度进步最大的村民（前10%）
 * 4. 帮扶榜查询：获取本季度净积分为负的村民（仅管理员可见）
 * 5. 个人数据查询：获取指定用户在指定季度的快照数据
 *
 * @author system
 * @since 2026-08-28
 */
public interface QuarterlySnapshotService extends IService<QuarterlySnapshot> {

    // ==================== 季度结算 ====================

    /**
     * 季度结算（核心方法）
     * 在每季度最后一天 23:59:59 由定时任务触发执行
     *
     * 执行流程：
     * 1. 获取该租户下所有村民用户
     * 2. 遍历计算每户的季度数据：
     *    a. quarter_earned_points（本季获得积分）
     *    b. quarter_net_points（本季净积分）
     *    c. rule_count（加分事项数量）
     *    d. activity_count（活动参与次数）
     *    e. no_penalty_days（连续零扣分天数）
     *    f. last_activity_time（最后一次积分变动时间）
     * 3. 计算上季度数据，得出 progress_points（进步分）
     * 4. 按4个维度降序排序，分配 rank_points（积分排名）
     * 5. 按进步分降序排序，分配 rank_progress（进步排名）
     * 6. 标记 tag（red/progress/normal/warning）
     * 7. 批量保存/更新快照数据
     *
     * @param quarter  季度标识，如：2026-Q3
     * @param tenantId 租户ID
     */
    void settleQuarter(String quarter, Integer tenantId);

    /**
     * 季度结算（异步版本）
     * 适用于数据量大时，避免阻塞主线程
     *
     * @param quarter  季度标识
     * @param tenantId 租户ID
     */
    void settleQuarterAsync(String quarter, Integer tenantId);

    // ==================== 榜单查询 ====================

    /**
     * 获取红榜列表
     * 红榜标准：tag = 'red'，按 rank_points 升序排列
     * 取前10%（最少3人，最多20人）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识（为 null 时获取当前季度）
     * @return 红榜列表
     */
    List<QuarterlySnapshot> getRedList(Integer tenantId, String quarter);

    /**
     * 获取红榜列表（带分页）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页红榜列表
     */
    List<QuarterlySnapshot> getRedList(Integer tenantId, String quarter, Integer pageNum, Integer pageSize);

    /**
     * 获取蜕变榜列表
     * 蜕变榜标准：tag = 'progress'，按 rank_progress 升序排列
     * 进步分为 0 或负数的家庭不进入
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识（为 null 时获取当前季度）
     * @return 蜕变榜列表
     */
    List<QuarterlySnapshot> getProgressList(Integer tenantId, String quarter);

    /**
     * 获取蜕变榜列表（带分页）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页蜕变榜列表
     */
    List<QuarterlySnapshot> getProgressList(Integer tenantId, String quarter, Integer pageNum, Integer pageSize);

    /**
     * 获取帮扶榜列表（管理员后台专用）
     * 帮扶榜标准：tag = 'warning'，即 quarter_net_points < 0
     * 按 quarter_net_points 升序排列（积分最低的最需要帮扶）
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识（为 null 时获取当前季度）
     * @return 帮扶榜列表
     */
    List<QuarterlySnapshot> getWarningList(Integer tenantId, String quarter);

    // ==================== 个人数据查询 ====================

    /**
     * 获取指定用户在指定季度的快照数据
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param userId   用户ID
     * @return 季度快照数据，不存在则返回 null
     */
    QuarterlySnapshot getUserQuarterData(Integer tenantId, String quarter, Integer userId);

    /**
     * 获取指定用户在指定季度的快照数据（带默认值）
     * 如果不存在，返回一个空的（所有字段为0）快照对象
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param userId   用户ID
     * @return 季度快照数据，不存在则返回默认空对象
     */
    QuarterlySnapshot getUserQuarterDataOrDefault(Integer tenantId, String quarter, Integer userId);

    /**
     * 获取指定用户在所有季度的快照数据（按季度降序）
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 季度快照列表
     */
    List<QuarterlySnapshot> getUserHistory(Integer tenantId, Integer userId);

    /**
     * 获取指定用户在指定季度的排名
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @param userId   用户ID
     * @return 排名（数字越小排名越高），未上榜返回 null
     */
    Integer getUserRank(Integer tenantId, String quarter, Integer userId);

    // ==================== 统计查询 ====================

    /**
     * 统计某季度红榜总人数
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 红榜人数
     */
    Integer countRedList(Integer tenantId, String quarter);

    /**
     * 统计某季度蜕变榜总人数
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 蜕变榜人数
     */
    Integer countProgressList(Integer tenantId, String quarter);

    /**
     * 统计某季度帮扶榜总人数
     *
     * @param tenantId 租户ID
     * @param quarter  季度标识
     * @return 帮扶榜人数
     */
    Integer countWarningList(Integer tenantId, String quarter);

    // ==================== 工具方法 ====================

    /**
     * 获取当前季度标识
     * 如：2026-Q3
     *
     * @return 当前季度标识
     */
    String getCurrentQuarter();

    /**
     * 获取指定日期的季度标识
     *
     * @param year  年份
     * @param month 月份（1-12）
     * @return 季度标识，如：2026-Q3
     */
    String getQuarterByDate(Integer year, Integer month);

    /**
     * 获取上季度标识
     *
     * @param quarter 当前季度标识，如：2026-Q3
     * @return 上季度标识，如：2026-Q2
     */
    String getPreviousQuarter(String quarter);

    /**
     * 获取季度开始时间
     *
     * @param quarter 季度标识，如：2026-Q3
     * @return 季度开始时间（该季度第一天 00:00:00）
     */
    java.time.LocalDateTime getQuarterStartTime(String quarter);

    /**
     * 获取季度结束时间
     *
     * @param quarter 季度标识，如：2026-Q3
     * @return 季度结束时间（该季度最后一天 23:59:59）
     */
    java.time.LocalDateTime getQuarterEndTime(String quarter);

    /**
     * 获取可用的季度列表（从最早有数据到当前季度）
     *
     * @param tenantId 租户ID
     * @return 季度标识列表，按时间降序排列
     */
    List<String> getAvailableQuarters(Integer tenantId);

    // ==================== 手动触发（管理员接口） ====================

    /**
     * 手动触发季度结算（管理员专用）
     * 用于补录或重新计算某季度数据
     *
     * @param quarter  季度标识
     * @param tenantId 租户ID
     * @param force    是否强制重新计算（true：删除已有数据重新计算）
     */
    void settleQuarterManual(String quarter, Integer tenantId, Boolean force);

    /**
     * 删除某季度的所有快照数据
     * 用于季度数据异常时重置
     *
     * @param quarter  季度标识
     * @param tenantId 租户ID
     * @return 删除记录数
     */
    Integer deleteQuarterData(String quarter, Integer tenantId);

    // ==================== 同名次排序辅助方法 ====================

    /**
     * 判断两个快照对象是否完全相同（用于确定是否并列）
     * 比较维度：quarter_earned_points, rule_count, activity_count, no_penalty_days, last_activity_time
     *
     * @param a 快照A
     * @param b 快照B
     * @return true：完全相同（应并列），false：不相同
     */
    boolean isSameRank(QuarterlySnapshot a, QuarterlySnapshot b);

}