package com.scau.village.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.scau.village.common.context.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 多租户拦截器（手动实现 TenantLineHandler）
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                UserContext ctx = UserContext.get();
                // 如果未登录或 tenantId 为空，默认使用租户 1
                Integer tenantId = (ctx != null) ? ctx.getTenantId() : 1;
                if (tenantId == null) {
                    tenantId = 1;
                }
                return new LongValue(tenantId);
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 租户表、用户表在超级管理员时不加 tenant_id 条件；其他表全部加
                if ("tenant".equals(tableName)) {
                    return true;
                }
                if ("user".equals(tableName) && isSuperAdmin()) {
                    return true;
                }
                return false;
            }

            private Integer getCurrentTenantId() {
                UserContext ctx = UserContext.get();
                return ctx != null ? ctx.getTenantId() : null;
            }

            private boolean isSuperAdmin() {
                UserContext ctx = UserContext.get();
                return ctx != null && "SUPER_ADMIN".equals(ctx.getRole());
            }
        }));

        // 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }
}