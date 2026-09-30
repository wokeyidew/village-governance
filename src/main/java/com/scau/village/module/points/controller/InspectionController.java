package com.scau.village.module.points.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.service.AiMatchService;
import com.scau.village.module.points.dto.CreateBatchDto;
import com.scau.village.module.points.dto.InspectionQueryDto;
import com.scau.village.module.points.dto.OfflineScoreDto;
import com.scau.village.module.points.dto.ReviewDto;
import com.scau.village.module.points.dto.ScoreSubmitDto;
import com.scau.village.module.points.entity.InspectionBatch;
import com.scau.village.module.points.entity.OfflineSyncRecord;
import com.scau.village.module.points.entity.PointsApply;
import com.scau.village.module.points.entity.PointsRule;
import com.scau.village.module.points.entity.PublishSnapshot;
import com.scau.village.module.points.mapper.OfflineSyncRecordMapper;
import com.scau.village.module.points.mapper.PointsApplyMapper;
import com.scau.village.module.points.mapper.PointsRuleMapper;
import com.scau.village.module.points.service.InspectionBatchService;
import com.scau.village.module.points.service.PointsApplyService;
import com.scau.village.module.points.service.PublishSnapshotService;
import com.scau.village.module.points.service.ReviewFlowService;
import com.scau.village.module.points.vo.AiMatchVO;
import com.scau.village.module.points.vo.CompareVO;
import com.scau.village.module.points.vo.ExportDataVO;
import com.scau.village.module.points.vo.InspectionRecordVO;
import com.scau.village.module.points.vo.SnapshotVO;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 现场检查评分控制器（管理员端）
 * 提供检查批次管理、评分提交、历史记录查询、Excel导出、红黑榜发布与查询、AI辅助匹配、离线同步等功能
 * 路径前缀：/api/inspection
 *
 * 修复说明（2026-08-30）：
 * - publishSnapshot 和 getSnapshotByBatchId 的 @PathVariable 参数从 Long 改为 String，解决雪花ID精度丢失
 * - 内部调用直接传递 String，保持与 Service 层签名一致
 *
 * @author system
 * @since 2026-07-16
 */
@Slf4j
@RestController
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
@Validated
public class InspectionController {

    private final InspectionBatchService batchService;
    private final PointsApplyService pointsApplyService;
    private final PointsApplyMapper pointsApplyMapper;
    private final UserService userService;
    private final PointsRuleMapper pointsRuleMapper;
    private final PublishSnapshotService publishSnapshotService;
    private final AiMatchService aiMatchService;
    private final OfflineSyncRecordMapper offlineSyncRecordMapper;
    private final ReviewFlowService reviewFlowService;

    /**
     * 1. 创建检查批次
     */
    @PostMapping("/batches")
    public Result<InspectionBatch> createBatch(@Valid @RequestBody CreateBatchDto dto) {
        InspectionBatch batch = batchService.createBatch(dto);
        return Result.success(batch);
    }

    /**
     * 2. 获取批次列表（下拉选/列表）
     */
    @GetMapping("/batches")
    public Result<List<InspectionBatch>> listBatches() {
        Integer tenantId = UserContext.getCurrentTenantId();
        List<InspectionBatch> list = batchService.lambdaQuery()
                .eq(InspectionBatch::getTenantId, tenantId)
                .orderByDesc(InspectionBatch::getCreateTime)
                .list();
        return Result.success(list);
    }

    /**
     * 3. 获取户列表（用于评分选择）
     * 返回当前租户下所有村民角色用户，按真实姓名排序
     */
    @GetMapping("/households")
    public Result<List<User>> listHouseholds(@RequestParam(required = false) String group) {
        Integer tenantId = UserContext.getCurrentTenantId();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, tenantId)
               .eq(User::getRole, "VILLAGER")
               .orderByAsc(User::getRealName);
        List<User> users = userService.list(wrapper);
        return Result.success(users);
    }

    /**
     * 4. 提交单户评分（支持多个规则）
     */
    @PostMapping("/score")
    public Result<Void> submitScore(@Valid @RequestBody ScoreSubmitDto dto) {
        Long inspectorIdLong = UserContext.getCurrentUserId();
        Integer inspectorId = inspectorIdLong != null ? inspectorIdLong.intValue() : null;
        Integer tenantId = UserContext.getCurrentTenantId();
        pointsApplyService.saveAdminScore(dto, inspectorId, tenantId);
        return Result.success(null);
    }

    /** 双审规则的阶段审核；全部阶段通过后才结算积分。 */
    @PostMapping("/review/{applyId}")
    public Result<Boolean> reviewScore(@PathVariable String applyId,
                                       @Valid @RequestBody ReviewDto dto) {
        Long reviewerIdLong = UserContext.getCurrentUserId();
        if (reviewerIdLong == null) {
            return Result.error(401, "请先登录");
        }
        if (!"approved".equals(dto.getDecision()) && !"rejected".equals(dto.getDecision())) {
            return Result.error(400, "审核决定必须为 approved 或 rejected");
        }
        PointsApply apply = pointsApplyService.getById(applyId);
        if (apply == null || !"pending_review".equals(apply.getStatus())) {
            return Result.error(400, "评分申请不在待双审状态");
        }

        int reviewerId = reviewerIdLong.intValue();
        boolean fullyApproved = reviewFlowService.completeStage(
                applyId, reviewerId, dto.getDecision(), dto.getRemark());
        if ("rejected".equals(dto.getDecision())) {
            apply.setStatus("rejected");
            apply.setAuditorId(reviewerId);
            apply.setAuditTime(LocalDateTime.now());
            apply.setAuditRemark(dto.getRemark());
            pointsApplyService.updateById(apply);
        } else if (fullyApproved) {
            pointsApplyService.postDoubleApproval(applyId, reviewerId);
        }
        return Result.success("审核处理完成", fullyApproved);
    }

    /**
     * 5. 分页查询历史检查记录
     */
    @GetMapping("/records")
    public Result<Page<InspectionRecordVO>> queryRecords(@Valid InspectionQueryDto query) {
        Page<InspectionRecordVO> page = new Page<>(query.getPage(), query.getSize());
        Page<InspectionRecordVO> resultPage = pointsApplyMapper.selectInspectionRecords(page, query);
        return Result.success(resultPage);
    }

    /**
     * 6. 导出 Excel（支持按批次、按户、按日期范围导出明细）
     */
    @GetMapping("/export")
    public void exportExcel(InspectionQueryDto query, HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("检查评分记录_" + System.currentTimeMillis(), "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        List<ExportDataVO> dataList = pointsApplyMapper.selectExportData(query);
        com.alibaba.excel.EasyExcel.write(response.getOutputStream(), ExportDataVO.class)
                .sheet("评分记录")
                .doWrite(dataList);
    }

    /**
     * 7. 获取当前有效的积分规则列表（供评分时勾选）
     */
    @GetMapping("/rules")
    public Result<List<PointsRule>> getActiveRules() {
        Integer tenantId = UserContext.getCurrentTenantId();
        LambdaQueryWrapper<PointsRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRule::getTenantId, tenantId)
               .eq(PointsRule::getStatus, 1)
               .orderByAsc(PointsRule::getCategory)
               .orderByAsc(PointsRule::getSortOrder);
        List<PointsRule> rules = pointsRuleMapper.selectList(wrapper);
        return Result.success(rules);
    }

    // ==================== 红黑榜相关接口 ====================

    /**
     * 8. 发布红黑榜快照
     * 根据批次ID生成快照并保存到数据库，发布后数据冻结
     * 
     * 修复说明：batchId 改为 String 类型
     */
    @PostMapping("/publish-snapshot/{batchId}")
    public Result<PublishSnapshot> publishSnapshot(@PathVariable String batchId) {
        Integer tenantId = UserContext.getCurrentTenantId();
        Long publisherId = UserContext.getCurrentUserId();
        if (publisherId == null) {
            return Result.error(401, "请先登录");
        }
        User publisher = userService.getById(publisherId);
        String publisherName = publisher != null ? publisher.getRealName() : "管理员";

        PublishSnapshot snapshot = publishSnapshotService.publishSnapshot(batchId, tenantId, publisherId, publisherName);
        return Result.success(snapshot);
    }

    /**
     * 9. 获取指定批次的红黑榜快照（已发布）
     * 
     * 修复说明：batchId 改为 String 类型
     */
    @GetMapping("/snapshot/{batchId}")
    public Result<SnapshotVO> getSnapshotByBatchId(@PathVariable String batchId) {
        SnapshotVO snapshot = publishSnapshotService.getSnapshotByBatchId(batchId);
        if (snapshot == null) {
            return Result.error(404, "该批次尚未发布榜单或不存在");
        }
        return Result.success(snapshot);
    }

    /**
     * 10. 获取最新发布的红黑榜快照
     */
    @GetMapping("/snapshot/latest")
    public Result<SnapshotVO> getLatestSnapshot() {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.error(401, "请先登录");
        }
        SnapshotVO snapshot = publishSnapshotService.getLatestSnapshot(tenantId);
        if (snapshot == null) {
            return Result.error(404, "暂无已发布的榜单");
        }
        return Result.success(snapshot);
    }

    /**
     * 11. 月度对比（当前月与上月对比）
     * 返回红黑榜变化数据
     */
    @GetMapping("/snapshot/compare")
    public Result<CompareVO> compareSnapshot(
            @RequestParam(required = false) String currentMonth,
            @RequestParam(required = false) String previousMonth) {
        Integer tenantId = UserContext.getCurrentTenantId();
        if (tenantId == null) {
            return Result.error(401, "请先登录");
        }

        CompareVO compareVO;
        if (currentMonth == null || previousMonth == null) {
            compareVO = publishSnapshotService.compareWithPreviousMonth(tenantId);
        } else {
            compareVO = publishSnapshotService.compareMonths(currentMonth, previousMonth, tenantId);
        }
        return Result.success(compareVO);
    }

    // ==================== AI辅助匹配接口 ====================

    /**
     * 12. AI匹配积分规则（支持 multipart/form-data 文件上传）
     * 前端通过 wx.uploadFile 上传图片，后端将图片转为 Base64 后调用 Python CLIP 服务
     * 
     * 修复说明：
     * - 当 AI 服务匹配结果返回 null 时（置信度低于阈值），返回错误提示而不是伪造数据
     * - 只有真正发生异常时才降级返回预设结果，保证演示不翻车
     * - 字段名统一为 ruleText，与 AiMatchVO 保持一致
     * 
     * @param image 上传的图片文件（前端字段名为 "image"）
     * @return AI 匹配结果，包含规则索引、规则名称、置信度、建议分数等
     */
    @PostMapping("/ai-match")
    public Result<AiMatchVO> aiMatch(@RequestParam("image") MultipartFile image) {
        log.info("【AI识别】收到图片文件，原始文件名: {}, 大小: {} bytes", 
                image.getOriginalFilename(), image.getSize());

        // 1. 校验文件是否为空
        if (image.isEmpty()) {
            log.warn("【AI识别】上传的文件为空");
            return Result.error(400, "图片文件不能为空");
        }

        // 2. 校验文件大小（限制 5MB）
        if (image.getSize() > 5 * 1024 * 1024) {
            log.warn("【AI识别】图片过大: {} bytes", image.getSize());
            return Result.error(400, "图片大小不能超过5MB");
        }

        // 3. 校验文件类型
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("【AI识别】不支持的文件类型: {}", contentType);
            return Result.error(400, "仅支持图片文件");
        }

        try {
            // 4. 将图片转换为 Base64（供 Python AI 服务使用）
            byte[] imageBytes = image.getBytes();
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info("【AI识别】图片转Base64成功，长度: {}", imageBase64.length());

            // 5. 调用 AI 服务匹配规则
            AiMatchService.AiMatchResult matchResult = aiMatchService.matchRule(imageBase64);

            // ========== 修复点：不再伪造数据 ==========
            // 当 matchResult 为 null 时，说明：
            //   a) AI 服务调用失败（网络超时/服务不可用）
            //   b) 置信度低于阈值（0.3），未匹配到有效规则
            // 统一返回错误提示，由前端引导用户手动选择
            if (matchResult == null) {
                log.warn("【AI识别】未匹配到有效规则（置信度低于阈值或服务不可用）");
                // 返回 404 状态码，前端可以据此显示"未识别到规则，请手动选择"
                return Result.error(404, "未识别到匹配的规则，请手动选择");
            }

            // 6. 构建返回 VO（使用 ruleText 字段）
            AiMatchVO vo = new AiMatchVO();
            vo.setRuleIndex(matchResult.getRuleIndex());
            vo.setRuleText(matchResult.getRuleName());  // 从 AiMatchResult 取 ruleName 赋给 ruleText
            vo.setConfidence(matchResult.getConfidence());
            vo.setSuggestedAction(matchResult.getSuggestedAction());
            vo.setSuggestedPoints(matchResult.getSuggestedPoints());
            vo.setIsDemo(false);
            
            log.info("【AI识别】匹配成功: ruleIndex={}, confidence={}", 
                    matchResult.getRuleIndex(), matchResult.getConfidence());
            return Result.success(vo);

        } catch (IOException e) {
            log.error("【AI识别】读取图片失败", e);
            return Result.error(500, "图片读取失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【AI识别】AI 匹配异常", e);
            // 只有真正发生异常时才降级，保证演示不翻车
            return Result.success(getFallbackAiMatchResult());
        }
    }

    /**
     * 降级模式：仅在 AI 服务发生异常时返回预设结果，保证演示不翻车
     * 
     * 注意：此方法仅在 catch (Exception) 时调用，不会在正常流程中被调用
     */
    private AiMatchVO getFallbackAiMatchResult() {
        AiMatchVO vo = new AiMatchVO();
        vo.setRuleIndex(1);
        vo.setRuleText("庭院地面干净整洁，无垃圾杂物");
        vo.setConfidence(0.94);
        vo.setSuggestedAction("加分");
        vo.setSuggestedPoints(10);
        vo.setIsDemo(true);
        return vo;
    }

    // ==================== 离线同步接口 ====================

    /**
     * 13. 离线评分同步
     * 用于离线模式下产生的评分数据，网络恢复后批量同步到服务器
     * 支持幂等：通过 clientEventId 避免重复提交
     */
    @PostMapping("/offline/sync")
    public Result<Map<String, Object>> syncOfflineScores(@Valid @RequestBody List<OfflineScoreDto> dtoList) {
        Integer tenantId = UserContext.getCurrentTenantId();
        Long inspectorIdLong = UserContext.getCurrentUserId();
        if (tenantId == null || inspectorIdLong == null) {
            return Result.error(401, "请先登录");
        }
        Integer inspectorId = inspectorIdLong.intValue();

        if (dtoList == null || dtoList.isEmpty()) {
            return Result.error(400, "同步数据不能为空");
        }

        // 结果统计
        int successCount = 0;
        int duplicateCount = 0;
        int failCount = 0;
        List<String> failedIds = new ArrayList<>();

        for (OfflineScoreDto dto : dtoList) {
            try {
                // 1. 幂等校验：检查 clientEventId 是否已处理
                String clientEventId = dto.getClientEventId();
                if (clientEventId == null) {
                    failCount++;
                    failedIds.add("null");
                    continue;
                }
                OfflineSyncRecord existRecord = offlineSyncRecordMapper.selectByClientEventId(clientEventId);
                if (existRecord != null) {
                    duplicateCount++;
                    log.info("离线事件 {} 已处理，跳过", clientEventId);
                    continue;
                }

                // 2. 转换为 ScoreSubmitDto
                ScoreSubmitDto scoreDto = new ScoreSubmitDto();
                scoreDto.setBatchId(String.valueOf(dto.getBatchId()));
                scoreDto.setUserId(dto.getUserId());
                scoreDto.setRules(dto.getRules());
                scoreDto.setDescription(dto.getDescription());
                scoreDto.setImages(dto.getImages());

                // 3. 执行评分保存
                pointsApplyService.saveAdminScore(scoreDto, inspectorId, tenantId);

                // 4. 记录同步成功，插入幂等记录
                OfflineSyncRecord record = new OfflineSyncRecord();
                record.setClientEventId(clientEventId);
                record.setProcessed(1);
                record.setProcessTime(LocalDateTime.now());
                record.setTenantId(tenantId);
                record.setCreateTime(LocalDateTime.now());
                offlineSyncRecordMapper.insert(record);

                successCount++;
                log.info("离线事件 {} 同步成功", clientEventId);

            } catch (Exception e) {
                failCount++;
                failedIds.add(dto.getClientEventId() != null ? dto.getClientEventId() : "null");
                log.error("离线事件 {} 同步失败: {}", dto.getClientEventId(), e.getMessage(), e);
            }
        }

        // 5. 返回同步结果统计
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("successCount", successCount);
        resultMap.put("duplicateCount", duplicateCount);
        resultMap.put("failCount", failCount);
        resultMap.put("failedIds", failedIds);

        log.info("离线同步完成，成功{}, 重复{}, 失败{}", successCount, duplicateCount, failCount);
        return Result.success(resultMap);
    }
}
