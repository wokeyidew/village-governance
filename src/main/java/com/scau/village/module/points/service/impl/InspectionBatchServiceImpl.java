// 文件路径: src/main/java/com/scau/village/module/points/service/impl/InspectionBatchServiceImpl.java
package com.scau.village.module.points.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.context.UserContext;
import com.scau.village.module.points.dto.CreateBatchDto;
import com.scau.village.module.points.entity.InspectionBatch;
import com.scau.village.module.points.mapper.InspectionBatchMapper;
import com.scau.village.module.points.service.InspectionBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 检查批次服务实现类
 *
 * @author system
 * @since 2026-07-16
 */
@Service
public class InspectionBatchServiceImpl
        extends ServiceImpl<InspectionBatchMapper, InspectionBatch>
        implements InspectionBatchService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionBatch createBatch(CreateBatchDto dto) {
        // 获取当前登录用户信息
        Long currentUserId = UserContext.getCurrentUserId();   // 返回 Long
        Integer tenantIdInt = UserContext.getCurrentTenantId(); // 返回 Integer

        InspectionBatch batch = new InspectionBatch();
        batch.setBatchName(dto.getBatchName());
        batch.setInspectionDate(dto.getInspectionDate());
        batch.setScope(dto.getScope());
        batch.setInspectorGroup(dto.getInspectorGroup());
        batch.setRemark(dto.getRemark());
        // tenantId 和 createBy 在实体中均为 Long，进行类型适配
        batch.setTenantId(tenantIdInt != null ? tenantIdInt.longValue() : null);
        batch.setCreateBy(currentUserId); // 直接赋值 Long
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        batch.setDeleted(0);

        save(batch);
        return batch;
    }
}