package com.scau.village.module.resident.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.resident.entity.ResidentProfile;
import com.scau.village.module.resident.service.ResidentProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 居民档案控制器
 * 提供居民档案查询（供注册时选择户主）和管理员导入 Excel 功能
 *
 * @author system
 * @since 2026-07-31
 */
@Slf4j
@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentProfileService residentProfileService;

    /**
     * 搜索户主（供注册时选择）
     * 公开接口，用于村民注册时根据关键词搜索户主
     *
     * @param keyword 搜索关键词（姓名/手机号/地址）
     * @return 匹配的居民档案列表（最多20条）
     */
    @GetMapping("/search")
    public Result<List<ResidentProfile>> search(@RequestParam String keyword) {
        // 获取租户ID，如果未登录则使用默认租户1
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;

        if (StringUtils.isBlank(keyword)) {
            return Result.success(new ArrayList<>());
        }

        List<ResidentProfile> list = residentProfileService.searchByKeyword(tenantId, keyword);
        return Result.success(list);
    }

    /**
     * 管理员导入居民档案 Excel
     * 权限：仅 VILLAGE_ADMIN
     *
     * @param file 上传的 Excel 文件（.xlsx 或 .xls）
     * @return 导入结果
     */
    @PostMapping("/admin/import")
    public Result<Void> importExcel(@RequestParam("file") MultipartFile file) {
        SecurityUtils.checkRole("VILLAGE_ADMIN");

        if (file.isEmpty()) {
            return Result.error(400, "文件为空，请上传有效的 Excel 文件");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error(400, "文件格式不正确，请上传 .xlsx 或 .xls 文件");
        }

        try {
            // 使用 EasyExcel 解析
            List<ResidentProfile> profiles = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), new ReadListener<Object>() {
                private int rowIndex = 0;

                @Override
                public void invoke(Object data, AnalysisContext context) {
                    rowIndex++;
                    // 跳过标题行（第一行）
                    if (rowIndex == 1) {
                        return;
                    }
                    // 将 data 转为 Map 或 List，根据实际 Excel 列顺序映射
                    // 这里假设数据为 List<Object>，按列顺序取值
                    if (data instanceof List) {
                        List<Object> row = (List<Object>) data;
                        if (row == null || row.isEmpty()) {
                            return;
                        }
                        // 根据实际 Excel 列索引映射（以用户提供的表格为例）
                        // 列索引：0-序号, 1-户主姓名, 2-人数, 3-区域, 4-所属区域, 5-联系方式, 6-地号, 7-门牌号, 8-同行政村搬, 9-原有村屋户主, 10-身份证号
                        // 注意：实际索引可能因 Excel 列位置变化，需根据实际情况调整
                        try {
                            ResidentProfile profile = new ResidentProfile();
                            // 获取 tenantId（当前管理员所属租户）
                            UserContext ctx = UserContext.get();
                            profile.setTenantId(ctx != null ? ctx.getTenantId() : 1);

                            // 户主姓名（索引1）
                            String ownerName = getString(row, 1);
                            if (StringUtils.isBlank(ownerName)) {
                                return; // 跳过空行
                            }
                            profile.setOwnerName(ownerName);

                            // 人数（索引2）
                            Integer totalPeople = getInteger(row, 2);
                            profile.setTotalPeople(totalPeople);

                            // 所属区域（索引3或4，取非空）
                            String area1 = getString(row, 3);
                            String area2 = getString(row, 4);
                            String villageGroup = StringUtils.isNotBlank(area1) ? area1 : area2;
                            profile.setVillageGroup(villageGroup);

                            // 联系方式（索引5）
                            String phone = getString(row, 5);
                            profile.setPhone(phone);

                            // 地号（索引6）
                            String address = getString(row, 6);
                            profile.setAddress(address);

                            // 身份证号（索引10）
                            String idCard = getString(row, 10);
                            profile.setIdCard(idCard);

                            // 其他字段可暂不填充
                            profiles.add(profile);
                        } catch (Exception e) {
                            log.warn("解析第 {} 行数据失败：{}", rowIndex, e.getMessage());
                        }
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 解析完成，共解析 {} 条数据", profiles.size());
                }
            }).sheet().doRead();

            // 批量保存
            if (!profiles.isEmpty()) {
                residentProfileService.saveBatch(profiles);
                log.info("成功导入 {} 条居民档案", profiles.size());
            } else {
                return Result.error(400, "未解析到有效数据，请检查 Excel 格式");
            }

            return Result.success(null);
        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            return Result.error(500, "读取文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("导入居民档案异常", e);
            return Result.error(500, "导入失败：" + e.getMessage());
        }
    }

    // ========== 辅助方法 ==========

    private String getString(List<Object> row, int index) {
        if (row.size() <= index) {
            return null;
        }
        Object value = row.get(index);
        return value == null ? null : value.toString().trim();
    }

    private Integer getInteger(List<Object> row, int index) {
        String str = getString(row, index);
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}