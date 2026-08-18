package com.scau.village.module.activity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.activity.dto.ActivitySignStatsDTO;
import com.scau.village.module.activity.dto.QRCodeDto;
import com.scau.village.module.activity.dto.RegistrationDetailVO;
import com.scau.village.module.activity.entity.Activity;
import com.scau.village.module.activity.entity.ActivityRegistration;
import com.scau.village.module.activity.service.ActivityRegistrationService;
import com.scau.village.module.activity.service.ActivityService;
import com.scau.village.module.activity.vo.QRCodeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 活动模块控制器
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityRegistrationService registrationService;

    /**
     * 分页获取活动列表（公开）
     * 返回时动态计算活动状态：0-未开始，1-进行中，2-已结束
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;
        Page<Activity> pageParam = new Page<>(page, size);
        Page<Activity> result = activityService.lambdaQuery()
                .eq(Activity::getTenantId, tenantId)
                .orderByDesc(Activity::getCreateTime)
                .page(pageParam);

        // 动态计算活动状态
        LocalDateTime now = LocalDateTime.now();
        for (Activity activity : result.getRecords()) {
            if (activity.getStartTime() != null && activity.getEndTime() != null) {
                if (activity.getStartTime().isAfter(now)) {
                    activity.setStatus(0); // 未开始
                } else if (activity.getEndTime().isBefore(now)) {
                    activity.setStatus(2); // 已结束
                } else {
                    activity.setStatus(1); // 进行中
                }
            }
        }

        return Result.success(result);
    }

    /**
     * 获取活动详情（公开）
     * 返回时动态计算活动状态：0-未开始，1-进行中，2-已结束
     */
    @GetMapping("/detail/{id}")
    public Result<Activity> getActivityDetail(@PathVariable Integer id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }

        // 动态计算活动状态
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            if (activity.getStartTime().isAfter(now)) {
                activity.setStatus(0); // 未开始
            } else if (activity.getEndTime().isBefore(now)) {
                activity.setStatus(2); // 已结束
            } else {
                activity.setStatus(1); // 进行中
            }
        }

        return Result.success(activity);
    }

    /**
     * 创建活动（仅村委管理员）
     * 修复：增加 tenantId 判空保护，防止空指针异常导致 500
     */
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody Activity activity) {
        log.info("收到创建活动请求: title={}, startTime={}, endTime={}, location={}, rewardPoints={}",
                activity.getTitle(), activity.getStartTime(), activity.getEndTime(),
                activity.getLocation(), activity.getRewardPoints());

        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            log.warn("创建活动失败: 用户未登录");
            return Result.error(401, "请先登录");
        }

        // tenantId 判空保护
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            log.error("创建活动失败: 当前用户租户ID为空，userId={}", ctx.getUserId());
            return Result.error(400, "租户信息缺失，请重新登录");
        }

        log.info("当前用户: userId={}, role={}, tenantId={}", ctx.getUserId(), ctx.getRole(), tenantId);

        SecurityUtils.checkRole("VILLAGE_ADMIN");

        activity.setTenantId(tenantId);
        activity.setStatus(1); // 创建时默认为进行中，查询时会动态计算
        activity.setCreateTime(LocalDateTime.now());

        activityService.save(activity);
        log.info("活动创建成功: id={}, title={}", activity.getId(), activity.getTitle());
        return Result.success(null);
    }

    /**
     * 报名活动（村民）
     */
    @PostMapping("/register/{activityId}")
    public Result<Void> register(@PathVariable Integer activityId,
                                 @RequestParam String name,
                                 @RequestParam String phone,
                                 @RequestParam(required = false) String remark) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        registrationService.register(userId, activityId, name, phone, remark);
        return Result.success(null);
    }

    /**
     * 取消报名（村民取消自己的报名）
     */
    @DeleteMapping("/register/{registrationId}")
    public Result<Void> cancel(@PathVariable Integer registrationId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Long userId = ctx.getUserId();
        registrationService.cancel(userId, registrationId);
        return Result.success(null);
    }

    /**
     * 获取当前用户的所有报名记录（我的报名）
     */
    @GetMapping("/my-registrations")
    public Result<List<ActivityRegistration>> getMyRegistrations() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer userId = ctx.getUserId().intValue();
        List<ActivityRegistration> registrations = registrationService.getByUserId(userId);
        return Result.success(registrations);
    }

    /**
     * 签到（村委/网格员核销签到，仅记录签到时间，不发放积分）
     */
    @PutMapping("/signin/{registrationId}")
    public Result<Void> signIn(@PathVariable Integer registrationId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        Long operatorId = ctx.getUserId();
        registrationService.signIn(registrationId, operatorId);
        return Result.success(null);
    }

    /**
     * 签退（村委/网格员核销签退，记录签退时间，计算参与时长，发放活动奖励积分）
     */
    @PutMapping("/checkout/{registrationId}")
    public Result<Void> checkout(@PathVariable Integer registrationId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");
        Long operatorId = ctx.getUserId();
        registrationService.checkout(registrationId, operatorId);
        return Result.success(null);
    }

    // ==================== 二维码相关接口 ====================

    /**
     * 生成活动二维码（管理员）
     * 支持生成签到/签退的固定码或动态码（10秒刷新）
     */
    @PostMapping("/qrcode")
    public Result<QRCodeVO> generateQRCode(@Valid @RequestBody QRCodeDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        QRCodeVO vo = activityService.generateQRCode(dto.getActivityId(), dto.getType(), dto.getDynamic());
        return Result.success(vo);
    }

    /**
     * 村民扫码签到（自助签到）
     * 扫码内容包含 activityId 和 type，自动完成签到
     */
    @PostMapping("/scan-signin")
    public Result<String> scanSignIn(@RequestBody Map<String, String> body) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        String scanData = body.get("scanData");
        if (scanData == null || scanData.isEmpty()) {
            return Result.error(400, "扫码数据不能为空");
        }
        String result = activityService.scanSignIn(scanData, ctx.getUserId().intValue());
        return Result.success(result);
    }

    /**
     * 村民扫码签退（自助签退）
     * 扫码内容包含 activityId 和 type，自动完成签退并发放积分
     */
    @PostMapping("/scan-checkout")
    public Result<String> scanCheckout(@RequestBody Map<String, String> body) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        String scanData = body.get("scanData");
        if (scanData == null || scanData.isEmpty()) {
            return Result.error(400, "扫码数据不能为空");
        }
        String result = activityService.scanCheckout(scanData, ctx.getUserId().intValue());
        return Result.success(result);
    }

    // ==================== 删除活动（管理员） ====================

    /**
     * 删除活动（仅管理员）
     * 将活动状态改为 2（已结束），作为软删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Integer id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN");

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }

        // 软删除：状态置为 2（已结束）
        activity.setStatus(2);
        activityService.updateById(activity);
        log.info("活动已删除（标记为已结束），id={}, adminId={}", id, ctx.getUserId());
        return Result.success(null);
    }

    // ==================== 管理员签到统计接口 ====================

    /**
     * 获取活动签到统计（管理员）
     * 返回：报名人数、已签到、已签退、签到率、签退率
     */
    @GetMapping("/admin/sign-stats/{activityId}")
    public Result<ActivitySignStatsDTO> getSignStats(@PathVariable Integer activityId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");

        // 检查活动是否存在
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }

        ActivitySignStatsDTO stats = registrationService.getSignStatsByActivityId(
                activityId, ctx.getTenantId()
        );
        if (stats == null || stats.getTotalRegistrations() == null || stats.getTotalRegistrations() == 0) {
            // 活动存在但暂无报名数据
            ActivitySignStatsDTO emptyStats = new ActivitySignStatsDTO();
            emptyStats.setActivityId(activityId);
            emptyStats.setActivityTitle(activity.getTitle());
            emptyStats.setTotalRegistrations(0);
            emptyStats.setSignedInCount(0);
            emptyStats.setCheckedOutCount(0);
            emptyStats.setSignInRate(0.0);
            emptyStats.setCheckOutRate(0.0);
            return Result.success(emptyStats);
        }

        // 计算签到率和签退率
        if (stats.getTotalRegistrations() != null && stats.getTotalRegistrations() > 0) {
            stats.setSignInRate(
                    stats.getSignedInCount() * 100.0 / stats.getTotalRegistrations()
            );
            stats.setCheckOutRate(
                    stats.getCheckedOutCount() * 100.0 / stats.getTotalRegistrations()
            );
        } else {
            stats.setSignInRate(0.0);
            stats.setCheckOutRate(0.0);
        }
        return Result.success(stats);
    }

    /**
     * 获取活动报名明细列表（管理员查看谁签到/签退了）
     * 返回：每个报名人的签到状态、签退状态、时间等
     */
    @GetMapping("/admin/registration-details/{activityId}")
    public Result<List<RegistrationDetailVO>> getRegistrationDetails(@PathVariable Integer activityId) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        SecurityUtils.checkRole("VILLAGE_ADMIN", "GRID_MEMBER");

        // 检查活动是否存在
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }

        List<RegistrationDetailVO> details = registrationService.getRegistrationDetailsByActivityId(
                activityId, ctx.getTenantId()
        );
        return Result.success(details);
    }
}