package com.scau.village.module.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.module.points.dto.InspectionQueryDto;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.vo.ExportDataVO;
import com.scau.village.module.points.vo.InspectionRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分申请/评分记录 Mapper 接口
 * 扩展支持管理员检查记录的查询与导出
 *
 * @author system
 * @since 2026-07-16
 */
@Mapper
public interface PointsApplyMapper extends BaseMapper<PointsApply> {

    /**
     * 分页查询检查记录（管理员评分记录），支持按批次、用户、日期范围筛选
     * 联表查询 user、inspection_batch、points_rule 获取关联信息
     *
     * @param page  分页对象
     * @param query 查询条件（包含 batchId、userId、startDate、endDate、sourceType）
     * @return 分页结果，每页数据为 InspectionRecordVO
     */
    Page<InspectionRecordVO> selectInspectionRecords(Page<InspectionRecordVO> page,
                                                     @Param("query") InspectionQueryDto query);

    /**
     * 导出检查记录明细（不分页），用于 Excel 导出
     * 支持按批次、用户、日期范围筛选
     *
     * @param query 查询条件
     * @return 导出数据列表（ExportDataVO）
     */
    List<ExportDataVO> selectExportData(@Param("query") InspectionQueryDto query);

    /**
     * 按户汇总导出数据（按批次或日期范围汇总每户总得分）
     * 此方法从 inspection_household 表查询，但为方便统一，放在此 Mapper 中
     * 也可单独在 InspectionHouseholdMapper 中定义
     *
     * @param query 查询条件（含 batchId、startDate、endDate）
     * @return 汇总导出数据列表（可定义新的 VO 或使用 Map）
     */
    // List<ExportSummaryVO> selectExportSummary(@Param("query") InspectionQueryDto query);
}