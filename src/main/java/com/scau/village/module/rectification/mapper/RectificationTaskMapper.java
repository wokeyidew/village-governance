package com.scau.village.module.rectification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.rectification.entity.RectificationTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 整改任务 Mapper 接口
 * 对应表名：rectification_task
 * 提供整改任务的增删改查、统计等操作
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数类型从 Long 改为 String，与实体类字段类型保持一致
 * - 包括：userId、batchId、applyId
 *
 * @author system
 * @since 2026-08-19
 */
@Mapper
public interface RectificationTaskMapper extends BaseMapper<RectificationTask> {

    /**
     * 根据用户ID查询所有整改任务（村民端）
     *
     * @param userId 用户ID（雪花ID字符串）
     * @return 整改任务列表
     */
    @Select("SELECT * FROM rectification_task WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<RectificationTask> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和状态查询整改任务（村民端）
     *
     * @param userId 用户ID（雪花ID字符串）
     * @param status 任务状态（pending/reviewing/resolved/overdue）
     * @return 整改任务列表
     */
    @Select("SELECT * FROM rectification_task WHERE user_id = #{userId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<RectificationTask> selectByUserIdAndStatus(@Param("userId") String userId,
                                                     @Param("status") String status);

    /**
     * 根据批次ID查询所有整改任务（管理员端）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 整改任务列表
     */
    @Select("SELECT * FROM rectification_task WHERE batch_id = #{batchId} AND deleted = 0 ORDER BY create_time DESC")
    List<RectificationTask> selectByBatchId(@Param("batchId") String batchId);

    /**
     * 根据批次ID和状态查询整改任务（管理员端）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @param status  任务状态
     * @return 整改任务列表
     */
    @Select("SELECT * FROM rectification_task WHERE batch_id = #{batchId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<RectificationTask> selectByBatchIdAndStatus(@Param("batchId") String batchId,
                                                       @Param("status") String status);

    /**
     * 根据积分申请记录ID查询整改任务
     *
     * @param applyId 积分申请记录ID（points_apply.id，雪花ID字符串）
     * @return 整改任务
     */
    @Select("SELECT * FROM rectification_task WHERE apply_id = #{applyId} AND deleted = 0")
    RectificationTask selectByApplyId(@Param("applyId") String applyId);

    /**
     * 查询所有待整改且已逾期的任务（用于定时任务）
     *
     * @return 逾期任务列表
     */
    @Select("SELECT * FROM rectification_task WHERE status = 'pending' AND deadline < NOW() AND deleted = 0")
    List<RectificationTask> selectOverdueTasks();

    /**
     * 统计某个用户指定状态的任务数量
     *
     * @param userId 用户ID（雪花ID字符串）
     * @param status 任务状态
     * @return 任务数量
     */
    @Select("SELECT COUNT(*) FROM rectification_task WHERE user_id = #{userId} AND status = #{status} AND deleted = 0")
    Long countByUserIdAndStatus(@Param("userId") String userId,
                                 @Param("status") String status);

    /**
     * 统计某个批次的整改任务总数
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 任务总数
     */
    @Select("SELECT COUNT(*) FROM rectification_task WHERE batch_id = #{batchId} AND deleted = 0")
    Long countByBatchId(@Param("batchId") String batchId);

    /**
     * 统计某个批次的整改完成率
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 完成率（0-100之间的整数）
     */
    @Select("SELECT ROUND(COUNT(CASE WHEN status = 'resolved' THEN 1 END) * 100.0 / COUNT(*), 0) " +
            "FROM rectification_task WHERE batch_id = #{batchId} AND deleted = 0")
    Integer calculateCompletionRate(@Param("batchId") String batchId);

    /**
     * 批量更新任务状态（用于定时任务标记逾期）
     *
     * @param status    新状态
     * @param oldStatus 旧状态
     * @return 更新行数
     */
    @Update("UPDATE rectification_task SET status = #{status}, update_time = NOW() " +
            "WHERE status = #{oldStatus} AND deadline < NOW() AND deleted = 0")
    int batchUpdateOverdueStatus(@Param("status") String status,
                                  @Param("oldStatus") String oldStatus);

    /**
     * 根据积分申请记录ID更新整改状态（用于申诉撤销时联动）
     *
     * @param applyId 积分申请记录ID（雪花ID字符串）
     * @param status  新状态
     * @return 更新行数
     */
    @Update("UPDATE rectification_task SET status = #{status}, update_time = NOW() WHERE apply_id = #{applyId} AND deleted = 0")
    int updateStatusByApplyId(@Param("applyId") String applyId,
                               @Param("status") String status);
}