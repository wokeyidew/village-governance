package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.points.entity.PointsFlow;
import com.scau.village.module.points.mapper.PointsFlowMapper;
import com.scau.village.module.points.service.PointsFlowService;
import com.scau.village.module.points.vo.PointsFlowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 积分流水 Service 实现
 *
 * @author system
 * @since 2026-07-18
 */
@Slf4j
@Service
public class PointsFlowServiceImpl extends ServiceImpl<PointsFlowMapper, PointsFlow> implements PointsFlowService {

    // 来源类型中文映射
    private static final Map<String, String> SOURCE_TYPE_MAP = new HashMap<>();

    static {
        SOURCE_TYPE_MAP.put("apply", "积分申报");
        SOURCE_TYPE_MAP.put("admin_inspection", "管理员评分");
        SOURCE_TYPE_MAP.put("exchange", "商品兑换");
        SOURCE_TYPE_MAP.put("activity", "活动奖励");
        SOURCE_TYPE_MAP.put("rectification", "整改奖励");
    }

    @Override
    public Page<PointsFlowVO> getFlowPage(Integer userId, Integer pageNum, Integer pageSize) {
        // 1. 获取当前租户 ID（v2.0.9 修复：多租户数据隔离）
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            log.warn("【积分流水】租户信息缺失，使用默认租户ID=1");
            tenantId = 1; // 降级处理
        }

        // 2. 构建分页对象
        Page<PointsFlow> page = new Page<>(pageNum, pageSize);

        // 3. 调用 Mapper 自定义分页查询（含关联批次信息）
        // 【修改点】增加 tenantId 参数，确保多租户数据隔离
        Page<PointsFlowVO> voPage = baseMapper.selectFlowPage(page, userId, tenantId);

        // 4. 补充 sourceTypeText（中文类型描述）
        voPage.getRecords().forEach(vo -> {
            String sourceType = vo.getSourceType();
            if (sourceType != null) {
                String text = SOURCE_TYPE_MAP.get(sourceType);
                vo.setSourceTypeText(text != null ? text : sourceType);
            }
        });

        log.debug("【积分流水】查询成功，userId={}, 总记录数={}", userId, voPage.getTotal());
        return voPage;
    }
}