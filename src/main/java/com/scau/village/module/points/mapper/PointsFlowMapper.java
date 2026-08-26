package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.vo.PointsFlowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 积分流水 Mapper 接口
 *
 * @author system
 * @since 2026-07-18
 */
@Mapper
public interface PointsFlowMapper extends BaseMapper<PointsFlow> {

    /**
     * 分页查询用户的积分流水（含关联批次信息）
     * 用于积分明细页面展示，关联 points_apply 和 inspection_batch 表获取批次信息
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 分页的积分流水视图对象
     */
    Page<PointsFlowVO> selectFlowPage(Page<PointsFlow> page, @Param("userId") Integer userId);
}