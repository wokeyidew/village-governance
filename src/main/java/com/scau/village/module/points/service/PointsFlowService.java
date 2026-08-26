package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.vo.PointsFlowVO;

/**
 * 积分流水 Service 接口
 *
 * @author system
 * @since 2026-07-18
 */
public interface PointsFlowService extends IService<PointsFlow> {

    /**
     * 分页获取用户积分流水（含关联批次信息）
     * 返回的 PointsFlowVO 中包含 batchId 和 batchName 字段
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 积分流水分页数据
     */
    Page<PointsFlowVO> getFlowPage(Integer userId, Integer pageNum, Integer pageSize);
}