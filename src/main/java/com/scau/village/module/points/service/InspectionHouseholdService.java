// 文件路径: src/main/java/com/scau/village/module/points/service/InspectionHouseholdService.java
package com.scau.village.module.points.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scau.village.module.points.entity.InspectionHousehold;

/**
 * 检查户汇总服务接口
 *
 * 修复说明（2026-08-30）：
 * - batchId 从 Long 改为 String，对应数据库字段 varchar(64)
 * - inspectorId 从 Integer 改为 String，对应数据库字段 varchar(64)
 * - userId 保持 Integer（数据库 int 类型）
 *
 * @author system
 * @since 2026-07-16
 */
public interface InspectionHouseholdService extends IService<InspectionHousehold> {

    /**
     * 更新或插入户汇总记录
     * 根据批次ID和用户ID判断是否存在，存在则更新总分和详情，否则新增
     *
     * @param batchId    批次ID（雪花ID字符串）
     * @param userId     用户ID（自增ID）
     * @param totalScore 总得分
     * @param detailJson 详细得分JSON（可为null）
     * @param inspectorId 检查人ID（管理员ID，雪花ID字符串）
     * @param remark     备注（可选）
     * @return 操作后的汇总记录
     */
    InspectionHousehold saveOrUpdateSummary(String batchId, Integer userId, Integer totalScore,
                                            String detailJson, String inspectorId, String remark);
}