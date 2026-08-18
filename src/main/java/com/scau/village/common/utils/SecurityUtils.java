package com.scau.village.common.utils;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

/**
 * 权限校验工具类
 * 支持从 UserContext 和 SecurityContextHolder 两种方式获取当前用户角色
 *
 * @author system
 * @since 2026-07-17
 */
public class SecurityUtils {

    /**
     * 检查当前用户是否具有指定角色之一
     * 优先从 UserContext 获取，若为空则从 SecurityContextHolder 获取
     *
     * @param allowedRoles 允许的角色列表
     * @throws BusinessException 如果未登录或无权限
     */
    public static void checkRole(String... allowedRoles) {
        // 1. 优先从 UserContext 获取
        UserContext ctx = UserContext.get();
        String role = null;

        if (ctx != null && ctx.getUserId() != null) {
            role = ctx.getRole();
        }

        // 2. 如果 UserContext 没有，从 SecurityContextHolder 获取
        if (role == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                if (authorities != null && !authorities.isEmpty()) {
                    // 提取第一个权限作为角色（通常只有一个角色）
                    String authority = authorities.iterator().next().getAuthority();
                    if (authority != null && authority.startsWith("ROLE_")) {
                        role = authority.substring(5); // 移除 "ROLE_"
                    } else {
                        role = authority;
                    }
                    // 如果获取到了角色，填充 UserContext 以便后续使用
                    if (role != null && auth.getPrincipal() instanceof Long) {
                        UserContext tmpCtx = new UserContext();
                        tmpCtx.setUserId((Long) auth.getPrincipal());
                        tmpCtx.setRole(role);
                        // tenantId 无法从 SecurityContext 获取，保留 null
                        UserContext.set(tmpCtx);
                    }
                }
            }
        }

        // 3. 如果仍然没有角色，抛出未登录异常
        if (role == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 4. 检查角色是否匹配
        for (String allowed : allowedRoles) {
            if (allowed.equals(role)) {
                return;
            }
        }
        throw new BusinessException(403, "权限不足，需要角色：" + String.join(", ", allowedRoles));
    }
}