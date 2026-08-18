package com.scau.village.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scau.village.common.context.UserContext;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.SecurityUtils;
import com.scau.village.module.user.dto.RegisterDto;
import com.scau.village.module.user.dto.UpdateProfileDto;
import com.scau.village.module.user.dto.UserVO;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 用户模块控制器
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户注册（支持头像、真实姓名）
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDto dto) {
        // 检查手机号是否已存在
        long count = userService.lambdaQuery()
                .eq(User::getPhone, dto.getPhone())
                .count();
        if (count > 0) {
            return Result.error(400, "手机号已注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setTenantId(dto.getTenantId());
        user.setRole("VILLAGER");
        user.setPoints(0);
        user.setRealName(dto.getRealName());   // 真实姓名
        user.setAvatar(dto.getAvatar());       // 头像URL（可为空）
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);
        return Result.success(null);
    }

    /**
     * 获取当前登录用户信息（已脱敏）
     * 返回 UserVO，包含头像和角色字段
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }

        Long userId = ctx.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());        // 脱敏在 getter 中处理
        vo.setRealName(user.getRealName());
        vo.setIdCard(user.getIdCard());
        vo.setPoints(user.getPoints());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());          // ✅ 返回角色字段
        vo.setResidentProfileId(user.getResidentProfileId()); // ✅ 返回关联档案ID

        return Result.success(vo);
    }

    /**
     * 更新个人资料（头像、真实姓名、手机号、密码）
     * 手机号需唯一性校验，修改密码需验证旧密码
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer userId = ctx.getUserId().intValue(); // 适配 Service 参数类型
        userService.updateProfile(userId, dto);
        return Result.success(null);
    }

    // ==================== 新增：个人信息更新（村民/管理员） ====================

    /**
     * 村民更新自己的个人信息（手机号、真实姓名、身份证号）
     * 包含格式校验（手机号、身份证、姓名长度）和手机号唯一性校验
     */
    @PutMapping("/update")
    public Result<Void> updateSelf(@RequestBody UpdateProfileDto dto) {
        UserContext ctx = UserContext.get();
        if (ctx == null || ctx.getUserId() == null) {
            return Result.error(401, "请先登录");
        }
        Integer userId = ctx.getUserId().intValue();
        // 调用 Service 方法，只允许更新手机号、姓名、身份证
        userService.updateUserInfo(userId, dto.getPhone(), dto.getRealName(), dto.getIdCard());
        return Result.success(null);
    }

    /**
     * 管理员更新任意用户信息
     * 权限：仅 VILLAGE_ADMIN
     */
    @PutMapping("/admin/update/{userId}")
    public Result<Void> adminUpdateUser(@PathVariable Integer userId,
                                        @Valid @RequestBody UpdateProfileDto dto) {
        // 校验管理员权限
        SecurityUtils.checkRole("VILLAGE_ADMIN");
        // 管理员调用专用方法，同样包含基本校验
        userService.adminUpdateUserInfo(userId, dto.getPhone(), dto.getRealName(), dto.getIdCard());
        return Result.success(null);
    }
}