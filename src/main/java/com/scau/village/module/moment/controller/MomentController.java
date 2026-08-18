package com.scau.village.module.moment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.module.moment.dto.CommentVO;
import com.scau.village.module.moment.dto.MomentDto;
import com.scau.village.module.moment.dto.MomentVO;
import com.scau.village.module.moment.entity.Moment;
import com.scau.village.module.moment.entity.MomentComment;
import com.scau.village.module.moment.service.MomentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 村民动态（朋友圈）控制器
 *
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/moment")
@RequiredArgsConstructor
public class MomentController {

    private final MomentService momentService;

    /**
     * 发布动态
     */
    @PostMapping("/publish")
    public Result<Moment> publish(@Valid @RequestBody MomentDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        Moment moment = momentService.publish(ctx.getUserId().intValue(), tenantId, dto);
        return Result.success(moment);
    }

    /**
     * 获取动态列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<MomentVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        UserContext ctx = UserContext.get();
        Integer tenantId = (ctx != null && ctx.getTenantId() != null) ? ctx.getTenantId() : 1;
        Integer userId = (ctx != null && ctx.getUserId() != null) ? ctx.getUserId().intValue() : null;
        Page<MomentVO> result = momentService.getMomentList(tenantId, userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取动态详情（含评论）
     */
    @GetMapping("/detail/{id}")
    public Result<MomentVO> detail(@PathVariable Long id) {
        UserContext ctx = UserContext.get();
        Integer userId = (ctx != null && ctx.getUserId() != null) ? ctx.getUserId().intValue() : null;
        MomentVO vo = momentService.getMomentDetail(id, userId);
        return Result.success(vo);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{id}")
    public Result<Boolean> like(@PathVariable Long id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        boolean result = momentService.toggleLike(id, ctx.getUserId().intValue(), tenantId);
        return Result.success(result);
    }

    /**
     * 发表评论
     */
    @PostMapping("/comment/{id}")
    public Result<MomentComment> comment(@PathVariable Long id,
                                         @RequestParam String content) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer tenantId = ctx.getTenantId();
        if (tenantId == null) {
            tenantId = 1;
        }
        MomentComment comment = momentService.comment(id, ctx.getUserId().intValue(), content, tenantId);
        return Result.success(comment);
    }

    /**
     * 获取动态的评论列表
     */
    @GetMapping("/comments/{id}")
    public Result<List<CommentVO>> comments(@PathVariable Long id) {
        List<CommentVO> list = momentService.getComments(id);
        return Result.success(list);
    }

    /**
     * 修改动态（仅作者本人可修改）
     */
    @PutMapping("/{id}")
    public Result<Moment> updateMoment(@PathVariable Long id,
                                       @Valid @RequestBody MomentDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Moment moment = momentService.updateMoment(id, ctx.getUserId().intValue(), dto);
        return Result.success(moment);
    }

    /**
     * 删除动态（作者本人或管理员可删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        String role = ctx.getRole();
        momentService.deleteMoment(id, ctx.getUserId().intValue(), role);
        return Result.success(null);
    }

    /**
     * 修改评论（仅评论作者本人可修改）
     */
    @PutMapping("/comment/{id}")
    public Result<MomentComment> updateComment(@PathVariable Long id,
                                               @RequestParam String content) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        MomentComment comment = momentService.updateComment(id, ctx.getUserId().intValue(), content);
        return Result.success(comment);
    }

    /**
     * 删除评论（作者本人或管理员可删除）
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        String role = ctx.getRole();
        momentService.deleteComment(id, ctx.getUserId().intValue(), role);
        return Result.success(null);
    }
}