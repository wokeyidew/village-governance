// 文件路径: src/main/java/com/scau/village/common/utils/ExcelExportUtils.java
package com.scau.village.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Excel导出工具类（基于EasyExcel）
 * 封装常用导出方法，支持响应流输出和文件输出
 *
 * @author system
 * @since 2026-07-16
 */
@Slf4j
public class ExcelExportUtils {

    /**
     * 导出Excel到HTTP响应（单sheet），用于前端下载
     *
     * @param response  HttpServletResponse
     * @param fileName  文件名（不含扩展名），支持中文
     * @param sheetName 工作表名称
     * @param dataList  数据列表
     * @param clazz     数据类（需使用@ExcelProperty注解字段）
     * @param <T>       数据类型泛型
     */
    public static <T> void exportToResponse(HttpServletResponse response,
                                            String fileName,
                                            String sheetName,
                                            List<T> dataList,
                                            Class<T> clazz) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 对文件名进行URL编码，支持中文
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition",
                    "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            // 使用EasyExcel写入
            EasyExcel.write(response.getOutputStream(), clazz)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 自动列宽
                    .sheet(sheetName)
                    .doWrite(dataList);
        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败", e);
        }
    }

    /**
     * 导出Excel到指定文件路径（非Web场景）
     *
     * @param filePath  文件完整路径（如：/tmp/export.xlsx）
     * @param sheetName 工作表名称
     * @param dataList  数据列表
     * @param clazz     数据类
     * @param <T>       数据类型泛型
     */
    public static <T> void exportToFile(String filePath,
                                        String sheetName,
                                        List<T> dataList,
                                        Class<T> clazz) {
        EasyExcel.write(filePath, clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(dataList);
    }
}