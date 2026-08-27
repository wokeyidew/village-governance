package com.scau.village.common.scheduler;

import com.scau.village.module.points.service.QuarterlySnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 季度结算定时任务
 *
 * 功能说明：
 * 在每季度最后一天 23:59:59 自动触发季度结算，
 * 计算所有村民的本季度积分数据，生成红榜、蜕变榜、帮扶榜。
 *
 * 执行流程：
 * 1. 获取当前季度标识（如：2026-Q3）
 * 2. 调用 QuarterlySnapshotService.settleQuarter() 执行结算
 * 3. 记录执行日志
 *
 * 积分体系说明（v2.0）：
 * - quarter_earned_points：本季获得积分（仅加分项）
 * - quarter_net_points：本季净积分（加分 - 扣分 + 整改恢复）
 * - progress_points：进步分（本季 - 上季）
 * - rank_points：积分排名（同名次按4个维度降序排列）
 * - rank_progress：进步排名（进步分降序）
 * - tag：red（红榜）/ progress（蜕变榜）/ warning（帮扶榜）/ normal（普通）
 *
 * @author system
 * @since 2026-08-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuarterlySettlementTask {

    private final QuarterlySnapshotService quarterlySnapshotService;

    /**
     * 定时任务：每季度最后一天 23:59:59 执行
     *
     * cron 表达式说明：
     * 秒 分 时 日 月 周
     * 59 59 23 31 3,6,9,12 ?  → 3月31日、6月30日、9月30日、12月31日 23:59:59
     *
     * 注意：2月28/29日、1月31日等季度末日期需要单独处理，
     * 实际生产环境建议在每季度最后一天的固定时间触发。
     *
     * 更稳健的方案：每天检查一次，如果是季度最后一天则触发。
     */
    @Scheduled(cron = "59 59 23 31 3,6,9,12 ?")
    public void settleCurrentQuarter() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int quarter = (month - 1) / 3 + 1;
        String quarterStr = year + "-Q" + quarter;

        Integer tenantId = 1; // 默认租户，实际可从配置读取或遍历所有租户

        log.info("【季度结算-定时任务】开始执行，quarter={}, tenantId={}, time={}",
                quarterStr, tenantId, LocalDateTime.now());

        try {
            quarterlySnapshotService.settleQuarter(quarterStr, tenantId);
            log.info("【季度结算-定时任务】✅ 执行成功，quarter={}, tenantId={}", quarterStr, tenantId);
        } catch (Exception e) {
            log.error("【季度结算-定时任务】❌ 执行失败，quarter={}, tenantId={}, error={}",
                    quarterStr, tenantId, e.getMessage(), e);
        }
    }

    /**
     * 每日检查并结算（更稳健的方案）
     * 每天 23:59:00 检查今天是否是季度最后一天，如果是则触发结算
     *
     * cron 表达式：59 59 23 * * ?
     */
    @Scheduled(cron = "59 59 23 * * ?")
    public void checkAndSettleIfQuarterEnd() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        // 判断是否为季度最后一天
        boolean isQuarterEnd = false;
        if (month == 3 && day == 31) isQuarterEnd = true;
        if (month == 6 && day == 30) isQuarterEnd = true;
        if (month == 9 && day == 30) isQuarterEnd = true;
        if (month == 12 && day == 31) isQuarterEnd = true;

        if (!isQuarterEnd) {
            return;
        }

        int year = today.getYear();
        int quarter = (month - 1) / 3 + 1;
        String quarterStr = year + "-Q" + quarter;
        Integer tenantId = 1;

        log.info("【季度结算-每日检查】今天是季度最后一天，开始结算，quarter={}, tenantId={}",
                quarterStr, tenantId);

        try {
            quarterlySnapshotService.settleQuarter(quarterStr, tenantId);
            log.info("【季度结算-每日检查】✅ 结算成功，quarter={}", quarterStr);
        } catch (Exception e) {
            log.error("【季度结算-每日检查】❌ 结算失败，quarter={}, error={}",
                    quarterStr, e.getMessage(), e);
        }
    }

}