package com.scau.village.module.points.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 导出Excel数据VO（明细行）
 * 每条记录对应一条评分明细，用于导出按批次、按日期范围等明细数据
 * 如需按户汇总导出，可使用 ExportSummaryVO（另建）
 *
 * @author system
 * @since 2026-07-16
 */
@Data
@ColumnWidth(20) // 设置默认列宽，配合EasyExcel
public class ExportDataVO {

    @ExcelProperty("批次名称")
    private String batchName;

    @ExcelProperty("检查日期")
    private LocalDate inspectionDate;

    @ExcelProperty("户主姓名")
    private String userName;

    @ExcelProperty("规则名称")
    private String ruleName;

    @ExcelProperty("得分")
    private Integer score;          // 正数为加分，负数为扣分

    @ExcelProperty("检查人")
    private String inspectorName;

    @ExcelProperty("备注")
    private String description;

    @ExcelProperty("提交时间")
    private LocalDateTime createTime;
}