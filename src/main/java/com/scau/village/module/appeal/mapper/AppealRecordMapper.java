package com.scau.village.module.appeal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.appeal.entity.AppealRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 申诉记录 Mapper 接口
 * 对应表名：appeal_record
 * 提供申诉记录的增删改查、统计等操作
 *
 * 修复说明（2026-08-30）：
 * - 所有雪花 ID 参数类型从 Long 改为 String，与实体类字段类型保持一致
 * - 包括：userId、applyId、batchId、id
 *
 * @author system
 * @since 2026-08-19
 */
@Mapper
public interface AppealRecordMapper extends BaseMapper<AppealRecord> {

    /**
     * 根据用户ID查询所有申诉记录（村民端）
     *
     * @param userId 用户ID（雪花ID字符串）
     * @return 申诉记录列表
     */
    @Select("SELECT * FROM appeal_record WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<AppealRecord> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和状态查询申诉记录（村民端）
     *
     * @param userId 用户ID（雪花ID字符串）
     * @param status 申诉状态（pending/resolved）
     * @return 申诉记录列表
     */
    @Select("SELECT * FROM appeal_record WHERE user_id = #{userId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<AppealRecord> selectByUserIdAndStatus(@Param("userId") String userId,
                                                @Param("status") String status);

    /**
     * 根据积分申请记录ID查询申诉记录
     *
     * @param applyId 积分申请记录ID（points_apply.id，雪花ID字符串）
     * @return 申诉记录
     */
    @Select("SELECT * FROM appeal_record WHERE apply_id = #{applyId} AND deleted = 0")
    AppealRecord selectByApplyId(@Param("applyId") String applyId);

    /**
     * 根据批次ID查询所有申诉记录（管理员端）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 申诉记录列表
     */
    @Select("SELECT * FROM appeal_record WHERE batch_id = #{batchId} AND deleted = 0 ORDER BY create_time DESC")
    List<AppealRecord> selectByBatchId(@Param("batchId") String batchId);

    /**
     * 根据批次ID和状态查询申诉记录（管理员端）
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @param status  申诉状态
     * @return 申诉记录列表
     */
    @Select("SELECT * FROM appeal_record WHERE batch_id = #{batchId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<AppealRecord> selectByBatchIdAndStatus(@Param("batchId") String batchId,
                                                 @Param("status") String status);

    /**
     * 查询所有待处理的申诉记录（管理员端）
     *
     * @param tenantId 租户ID
     * @return 待处理申诉列表
     */
    @Select("SELECT * FROM appeal_record WHERE tenant_id = #{tenantId} AND status = 'pending' AND deleted = 0 ORDER BY create_time ASC")
    List<AppealRecord> selectPendingAppeals(@Param("tenantId") Integer tenantId);

    /**
     * 统计某个用户指定状态的申诉数量
     *
     * @param userId 用户ID（雪花ID字符串）
     * @param status 申诉状态
     * @return 申诉数量
     */
    @Select("SELECT COUNT(*) FROM appeal_record WHERE user_id = #{userId} AND status = #{status} AND deleted = 0")
    Long countByUserIdAndStatus(@Param("userId") String userId,
                                 @Param("status") String status);

    /**
     * 统计某个批次的申诉总数
     *
     * @param batchId 批次ID（雪花ID字符串）
     * @return 申诉总数
     */
    @Select("SELECT COUNT(*) FROM appeal_record WHERE batch_id = #{batchId} AND deleted = 0")
    Long countByBatchId(@Param("batchId") String batchId);

    /**
     * 更新申诉状态（用于管理员处理申诉后）
     *
     * @param id     申诉记录ID（雪花ID字符串）
     * @param status 新状态
     * @return 更新行数
     */
    @Update("UPDATE appeal_record SET status = #{status}, update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") String id,
                     @Param("status") String status);

    /**
     * 根据积分申请记录ID更新申诉状态（用于申诉撤销或修改时联动）
     *
     * @param applyId 积分申请记录ID（雪花ID字符串）
     * @param status  新状态
     * @return 更新行数
     */
    @Update("UPDATE appeal_record SET status = #{status}, update_time = NOW() WHERE apply_id = #{applyId} AND deleted = 0")
    int updateStatusByApplyId(@Param("applyId") String applyId,
                               @Param("status") String status);
}