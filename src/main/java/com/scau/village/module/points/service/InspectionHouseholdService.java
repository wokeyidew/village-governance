// 文件路径: src/main/java/com/scau/village/module/points/service/InspectionHouseholdService.java
package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.InspectionHousehold;

/**
 * 检查户汇总服务接口
 *
 * @author system
 * @since 2026-07-16
 */
public interface InspectionHouseholdService extends IService<InspectionHousehold> {

    /**
     * 更新或插入户汇总记录
     * 根据批次ID和用户ID判断是否存在，存在则更新总分和详情，否则新增
     *
     * @param batchId    批次ID
     * @param userId     用户ID
     * @param totalScore 总得分
     * @param detailJson 详细得分JSON（可为null）
     * @param inspectorId 检查人ID
     * @param remark     备注（可选）
     * @return 操作后的汇总记录
     */
    InspectionHousehold saveOrUpdateSummary(Long batchId, Integer userId, Integer totalScore,
                                            String detailJson, Integer inspectorId, String remark);
}