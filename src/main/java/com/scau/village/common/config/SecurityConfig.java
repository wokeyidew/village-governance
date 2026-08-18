package com.scau.village.common.config;

import com.scau.village.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Spring Security 配置
 * 使用 JWT 进行认证，基于角色进行授权
 */
@Slf4j
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtUtils jwtUtils;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                // ============ 绝对公开接口（无需认证，无需角色） ============
                .antMatchers(
                        "/api/auth/**",              // 登录、微信登录、微信注册
                        "/api/user/register",        // 注册
                        "/api/upload/**",            // 文件上传
                        "/api/file/**",              // 通用文件上传
                        "/api/points/rules",         // 积分规则（村民端）
                        "/api/shop/products",        // 商品列表（公开）
                        "/api/moment/list",          // 动态列表（公开）
                        "/api/moment/detail/**",     // 动态详情（公开）
                        "/api/moment/comments/**",   // 动态评论列表（公开）
                        "/api/residents/search",     // 搜索户主（注册时使用）
                        "/static/**",                // 静态资源
                        "/upload/**",                // 上传文件访问
                        "/actuator/**",              // 健康检查
                        "/favicon.ico"
                ).permitAll()

                // ============ 通知模块（GET 公开，POST/PUT/DELETE 需管理员） ============
                .antMatchers(HttpMethod.GET, "/api/notice/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/notice/publish").hasRole("VILLAGE_ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/notice/**").hasRole("VILLAGE_ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/notice/**").hasRole("VILLAGE_ADMIN")

                // ============ 政策模块（GET 公开，POST/PUT/DELETE 需管理员） ============
                .antMatchers(HttpMethod.GET, "/api/policy/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/policy/publish").hasRole("VILLAGE_ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/policy/**").hasRole("VILLAGE_ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/policy/**").hasRole("VILLAGE_ADMIN")

                // ============ 活动模块（GET 公开，POST/PUT/DELETE 需认证或管理员） ============
                .antMatchers(HttpMethod.GET, "/api/activity/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/activity/create").hasRole("VILLAGE_ADMIN")
                .antMatchers("/api/activity/register/**").authenticated()
                .antMatchers("/api/activity/my-registrations").authenticated()
                .antMatchers("/api/activity/signin/**").hasRole("VILLAGE_ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/activity/**").hasRole("VILLAGE_ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/activity/**").hasRole("VILLAGE_ADMIN")

                // ============ 积分超市兑换（需要认证） ============
                .antMatchers("/api/shop/exchange/**").authenticated()

                // ============ 动态模块（除了公开的 GET 之外，其他需要认证） ============
                .antMatchers(HttpMethod.POST, "/api/moment/publish").authenticated()
                .antMatchers(HttpMethod.POST, "/api/moment/like/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/moment/comment/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/moment/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/moment/**").authenticated()

                // ============ 检查评分接口（仅管理员） ============
                .antMatchers("/api/inspection/**").hasRole("VILLAGE_ADMIN")

                // ============ 积分管理（仅管理员） ============
                .antMatchers("/api/points/admin/**").hasRole("VILLAGE_ADMIN")

                // ============ 反馈模块（管理员部分） ============
                .antMatchers("/api/feedback/admin/**").hasRole("VILLAGE_ADMIN")
                // 村民端的反馈接口（提交、我的列表、详情）默认通过 anyRequest().authenticated() 保护，无需额外配置

                // ============ 居民档案模块（管理员导入） ============
                .antMatchers("/api/residents/admin/**").hasRole("VILLAGE_ADMIN")

                // ============ 用户信息更新（村民本人或管理员） ============
                .antMatchers("/api/user/update").authenticated()
                .antMatchers("/api/user/admin/**").hasRole("VILLAGE_ADMIN")

                // ============ 其他所有请求需要认证 ============
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * JWT 认证过滤器（从请求头中提取 token 并设置认证信息）
     * 使用 OncePerRequestFilter 确保每次请求只执行一次
     */
    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                // 从请求头获取 token（使用配置的 header 名称，如 "Authorization"）
                String authHeader = request.getHeader(jwtUtils.getHeader());
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        // 解析 token 获取用户信息
                        Claims claims = jwtUtils.getClaimsFromToken(token);
                        if (claims != null) {
                            Long userId = jwtUtils.getUserId(token);
                            String role = jwtUtils.getRole(token);

                            if (userId != null && role != null) {
                                // 构造 Spring Security 的认证对象
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userId, null,
                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                                        );
                                // 存入 SecurityContext
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                log.debug("JWT认证成功: userId={}, role={}", userId, role);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("JWT认证失败: {}", e.getMessage());
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}