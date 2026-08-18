package com.scau.village.common.context;

import lombok.Data;

/**
 * 用户上下文工具类
 * 基于 ThreadLocal 存储当前登录用户的信息，包括用户ID、角色、租户ID等
 * 在请求拦截器中设置，在请求结束后清除，确保线程安全
 *
 * @author system
 * @since 2026-08-14
 */
@Data
public class UserContext {
    private Long userId;
    private String role;
    private Integer tenantId;

    private static final ThreadLocal<UserContext> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前线程的用户上下文
     *
     * @param context 用户上下文对象
     */
    public static void set(UserContext context) {
        THREAD_LOCAL.set(context);
    }

    /**
     * 获取当前线程的用户上下文
     *
     * @return 用户上下文对象，若未设置则返回 null
     */
    public static UserContext get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 清除当前线程的用户上下文
     * 在请求结束后调用，防止内存泄漏
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }

    /**
     * 获取当前登录用户ID（静态快捷方法）
     *
     * @return 用户ID，若未登录则返回 null
     */
    public static Long getCurrentUserId() {
        UserContext context = get();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前租户ID（静态快捷方法）
     * 若上下文未设置或租户ID为 null，返回默认值 1
     *
     * @return 租户ID，默认值为 1
     */
    public static Integer getCurrentTenantId() {
        UserContext context = get();
        if (context == null || context.getTenantId() == null) {
            return 1; // 默认租户ID（龙胜村）
        }
        return context.getTenantId();
    }

    // ==================== 手动 getter/setter（确保 Lombok 未生效时编译通过） ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }
}