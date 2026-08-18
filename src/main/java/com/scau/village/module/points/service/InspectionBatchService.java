// 文件路径: src/main/java/com/scau/village/module/points/service/InspectionBatchService.java
package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.dto.CreateBatchDto;
import com.scau.village.module.points.entity.InspectionBatch;

/**
 * 检查批次服务接口
 *
 * @author system
 * @since 2026-07-16
 */
public interface InspectionBatchService extends IService<InspectionBatch> {

    /**
     * 创建检查批次
     *
     * @param dto 创建参数
     * @return 新创建的批次对象（含ID）
     */
    InspectionBatch createBatch(CreateBatchDto dto);
}