package com.scau.village.module.moment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.moment.entity.MomentComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 动态评论 Mapper 接口
 *
 * @author system
 * @since 2026-07-17
 */
@Mapper
public interface MomentCommentMapper extends BaseMapper<MomentComment> {

    /**
     * 根据动态ID查询所有评论
     *
     * @param momentId 动态ID
     * @return 评论列表
     */
    List<MomentComment> selectByMomentId(@Param("momentId") Long momentId);
}