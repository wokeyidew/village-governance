package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.points.entity.ScoreEvidence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评分证据 Mapper 接口
 * 对应表名：score_evidence
 * 提供证据的增删改查操作
 *
 * @author system
 * @since 2026-08-18
 */
@Mapper
public interface ScoreEvidenceMapper extends BaseMapper<ScoreEvidence> {

    /**
     * 根据积分申请/评分记录ID查询证据
     *
     * @param applyId 积分申请/评分记录ID（points_apply.id）
     * @return 证据对象，不存在则返回 null
     */
    @Select("SELECT * FROM score_evidence WHERE apply_id = #{applyId} AND deleted = 0")
    ScoreEvidence selectByApplyId(@Param("applyId") Long applyId);

    /**
     * 根据检查批次ID查询所有证据列表
     *
     * @param batchId 检查批次ID（inspection_batch.id）
     * @return 证据列表，若无证据则返回空列表
     */
    @Select("SELECT * FROM score_evidence WHERE batch_id = #{batchId} AND deleted = 0 ORDER BY create_time DESC")
    List<ScoreEvidence> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据检查人ID查询证据列表
     *
     * @param inspectorId 检查人ID（管理员ID）
     * @return 证据列表，若无证据则返回空列表
     */
    @Select("SELECT * FROM score_evidence WHERE inspector_id = #{inspectorId} AND deleted = 0 ORDER BY create_time DESC")
    List<ScoreEvidence> selectByInspectorId(@Param("inspectorId") Long inspectorId);

    /**
     * 批量查询证据（根据多个 applyId）
     *
     * @param applyIds 积分申请/评分记录ID列表
     * @return 证据列表，若无匹配则返回空列表
     */
    @Select("<script>" +
            "SELECT * FROM score_evidence WHERE apply_id IN " +
            "<foreach collection='applyIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0 ORDER BY create_time DESC" +
            "</script>")
    List<ScoreEvidence> selectByApplyIds(@Param("applyIds") List<Long> applyIds);

    /**
     * 根据积分申请/评分记录ID删除证据（逻辑删除）
     *
     * @param applyId 积分申请/评分记录ID
     * @return 影响行数
     */
    @Select("UPDATE score_evidence SET deleted = 1 WHERE apply_id = #{applyId}")
    int deleteByApplyId(@Param("applyId") Long applyId);

    /**
     * 统计某个批次的证据总数
     *
     * @param batchId 检查批次ID
     * @return 证据总数
     */
    @Select("SELECT COUNT(*) FROM score_evidence WHERE batch_id = #{batchId} AND deleted = 0")
    Long countByBatchId(@Param("batchId") Long batchId);

    /**
     * 统计某个检查人的证据总数
     *
     * @param inspectorId 检查人ID
     * @return 证据总数
     */
    @Select("SELECT COUNT(*) FROM score_evidence WHERE inspector_id = #{inspectorId} AND deleted = 0")
    Long countByInspectorId(@Param("inspectorId") Long inspectorId);
}