package com.scau.village.common.interceptor;

import com.scau.village.common.context.UserContext;
import com.scau.village.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 拦截器
 * 用于从请求头中提取 Token 并设置用户上下文
 * 白名单路径放行（无需 Token），但仅对公开的 GET 请求生效，其他方法需携带 Token
 *
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    /**
     * 绝对公开路径（无需 Token，无论请求方法）
     */
    private static final List<String> ABSOLUTE_WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/user/register",
            "/api/points/rules",
            "/api/shop/products",
            "/api/moment/list",
            "/api/moment/detail/",
            "/api/moment/comments/",
            "/api/upload/",
            "/api/file/",           // ✅ 添加此行，使文件上传接口公开
            "/static/",
            "/actuator/",
            "/favicon.ico"
    );

    /**
     * 需要区分请求方法的路径前缀：
     * GET 请求公开，其他方法需要认证
     */
    private static final List<String> METHOD_WHITE_PREFIXES = Arrays.asList(
            "/api/notice/",
            "/api/policy/",
            "/api/activity/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. 检查是否在绝对白名单中
        for (String path : ABSOLUTE_WHITE_LIST) {
            if (uri.startsWith(path)) {
                // 绝对公开路径，尝试解析 token（如果存在则设置上下文），但无论 token 是否有效都放行
                processTokenIfExists(request);
                log.debug("绝对白名单放行: uri={}, method={}", uri, method);
                return true;
            }
        }

        // 2. 检查是否在需要区分方法的路径前缀中
        for (String prefix : METHOD_WHITE_PREFIXES) {
            if (uri.startsWith(prefix)) {
                if ("GET".equalsIgnoreCase(method)) {
                    // GET 请求：公开，尝试解析 token（如果有），但无论 token 是否有效都放行
                    processTokenIfExists(request);
                    log.debug("GET 白名单放行: uri={}, method={}", uri, method);
                    return true;
                } else {
                    // 非 GET 请求（POST/PUT/DELETE）：需要认证，必须解析 token 且有效
                    log.debug("需要认证: uri={}, method={}", uri, method);
                    return processTokenRequired(request, response);
                }
            }
        }

        // 3. 其他路径：必须携带有效 Token
        log.debug("其他路径，需要认证: uri={}, method={}", uri, method);
        return processTokenRequired(request, response);
    }

    /**
     * 尝试解析 token，如果存在且有效则设置上下文，否则不设置（仍放行）
     */
    private void processTokenIfExists(HttpServletRequest request) {
        String token = request.getHeader(jwtUtils.getHeader());
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtils.validateToken(token)) {
                setUserContext(token);
                log.debug("Token 有效，已设置 UserContext");
            } else {
                log.debug("Token 无效，但路径公开，继续放行");
            }
        }
    }

    /**
     * 必须携带有效 Token，否则返回 false（拦截）
     */
    private boolean processTokenRequired(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(jwtUtils.getHeader());
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtils.validateToken(token)) {
                setUserContext(token);
                log.debug("Token 验证通过，UserContext 已设置");
                return true;
            } else {
                log.warn("Token 无效: {}", token);
            }
        } else {
            log.warn("请求头缺少 Authorization 或格式错误");
        }
        // Token 无效或缺失，设置 401 状态并拦截
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    /**
     * 设置用户上下文
     */
    private void setUserContext(String token) {
        UserContext context = new UserContext();
        context.setUserId(jwtUtils.getUserId(token));
        context.setRole(jwtUtils.getRole(token));
        context.setTenantId(jwtUtils.getTenantId(token));
        UserContext.set(context);
        log.debug("UserContext 已设置: userId={}, role={}, tenantId={}",
                context.getUserId(), context.getRole(), context.getTenantId());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}