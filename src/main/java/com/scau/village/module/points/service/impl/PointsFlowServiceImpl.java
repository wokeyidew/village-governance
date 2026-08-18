package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.points.service.PointsFlowService;
import org.springframework.stereotype.Service;

/**
 * 积分流水 Service 实现
 */
@Service
public class PointsFlowServiceImpl extends ServiceImpl<PointsFlowMapper, PointsFlow> implements PointsFlowService {
}