package com.scau.village.module.moment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scau.village.module.moment.entity.MomentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态点赞 Mapper 接口
 *
 * @author system
 * @since 2026-07-17
 */
@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {
}