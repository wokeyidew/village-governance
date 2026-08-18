package com.scau.village.module.moment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.moment.dto.MomentVO;
import com.scau.village.module.moment.entity.Moment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 动态 Mapper 接口
 *
 * @author system
 * @since 2026-07-17
 */
@Mapper
public interface MomentMapper extends BaseMapper<Moment> {

    /**
     * 分页查询动态列表（含点赞数、评论数、是否已点赞）
     *
     * @param page     分页对象
     * @param tenantId 租户ID
     * @param userId   当前用户ID（用于判断是否已点赞）
     * @return 分页结果
     */
    Page<MomentVO> selectMomentList(Page<MomentVO> page,
                                    @Param("tenantId") Integer tenantId,
                                    @Param("userId") Integer userId);
}