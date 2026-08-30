package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.PublishSnapshot;
import com.scau.village.module.points.vo.SnapshotVO;
import com.scau.village.module.points.vo.CompareVO;

import java.util.List;

/**
 * 红黑榜公示快照服务接口
 * 对应表名：publish_snapshot
 * 提供快照的生成、发布、查询、对比等业务功能
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数类型从 Long 改为 String
 * - 包括：batchId、snapshotId、userId
 * - 与 Controller 层 String 类型保持一致，解决前端雪花ID精度丢失问题
 *
 * @author system
 * @since 2026-08-19
 */
public interface PublishSnapshotService extends IService<PublishSnapshot> {

    // ==================== 生成与发布方法 ====================

    /**
     * 生成红黑榜快照数据（不保存到数据库，仅返回计算结果）
     * 根据批次ID计算所有户的排名、分数和标签
     *
     * @param batchId  批次ID（雪花ID字符串）
     * @param tenantId 租户ID
     * @return 快照视图对象
     */
    SnapshotVO generateSnapshot(String batchId, Integer tenantId);

    /**
     * 发布红黑榜快照
     * 生成快照数据并保存到数据库，发布后数据冻结
     *
     * @param batchId       批次ID（雪花ID字符串）
     * @param tenantId      租户ID
     * @param publisherId   发布人ID（管理员，雪花ID字符串）
     * @param publisherName 发布人姓名
     * @return 保存后的快照对象
     */
    PublishSnapshot publishSnapshot(String batchId, Integer tenantId, Long publisherId, String publisherName);

    // ==================== 查询方法 ====================

    /**
     * 根据批次ID查询快照
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 快照对象，不存在返回 null
     */
    PublishSnapshot getByBatchId(String batchId);

    /**
     * 根据批次ID查询快照并转换为VO
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 快照视图对象，不存在返回 null
     */
    SnapshotVO getSnapshotByBatchId(String batchId);

    /**
     * 根据月份查询快照
     *
     * @param month    月份，格式：yyyy-MM
     * @param tenantId 租户ID
     * @return 快照列表
     */
    List<SnapshotVO> getSnapshotsByMonth(String month, Integer tenantId);

    /**
     * 获取最新发布的快照
     *
     * @param tenantId 租户ID
     * @return 最新快照视图对象，不存在返回 null
     */
    SnapshotVO getLatestSnapshot(Integer tenantId);

    /**
     * 获取某个月份之前的所有快照（用于月度对比）
     *
     * @param tenantId 租户ID
     * @param month    当前月份，格式：yyyy-MM
     * @return 之前的快照列表
     */
    List<SnapshotVO> getSnapshotsBeforeMonth(Integer tenantId, String month);

    // ==================== 月度对比方法 ====================

    /**
     * 获取月度对比数据
     * 对比两个月份的红黑榜变化
     *
     * @param currentMonth  当前月份，格式：yyyy-MM
     * @param previousMonth 对比月份，格式：yyyy-MM
     * @param tenantId      租户ID
     * @return 对比视图对象
     */
    CompareVO compareMonths(String currentMonth, String previousMonth, Integer tenantId);

    /**
     * 获取当前月与上月的对比数据（便捷方法）
     *
     * @param tenantId 租户ID
     * @return 对比视图对象
     */
    CompareVO compareWithPreviousMonth(Integer tenantId);

    /**
     * 获取某个用户在两个月份之间的排名变化
     *
     * @param userId     用户ID（雪花ID字符串）
     * @param startMonth 起始月份，格式：yyyy-MM
     * @param endMonth   结束月份，格式：yyyy-MM
     * @param tenantId   租户ID
     * @return 排名变化信息
     */
    CompareVO.RankChange getUserRankChange(String userId, String startMonth, String endMonth, Integer tenantId);

    // ==================== 删除方法 ====================

    /**
     * 根据批次ID删除快照（逻辑删除）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 是否删除成功
     */
    boolean deleteByBatchId(String batchId);

    /**
     * 物理删除快照（慎用）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 是否删除成功
     */
    boolean forceDeleteByBatchId(String batchId);

    // ==================== 统计方法 ====================

    /**
     * 统计某个批次是否已发布快照
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return true-已发布，false-未发布
     */
    boolean isPublished(String batchId);

    /**
     * 获取某个快照的红榜户数
     *
     * @param snapshotId 快照ID（雪花ID字符串）
     * @return 红榜户数
     */
    Integer getRedCount(String snapshotId);

    /**
     * 获取某个快照的黑榜户数
     *
     * @param snapshotId 快照ID（雪花ID字符串）
     * @return 黑榜户数
     */
    Integer getBlackCount(String snapshotId);

    /**
     * 统计某个月份的参与户数
     *
     * @param month    月份，格式：yyyy-MM
     * @param tenantId 租户ID
     * @return 参与户数
     */
    Integer getParticipantCount(String month, Integer tenantId);
}