package com.scau.village.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * 允许小程序前端跨域访问后端 API
 * 
 * @author system
 * @since 2026-07-17
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                          // 允许所有路径
                .allowedOriginPatterns("*")                 // 允许所有域名（生产环境应指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                .allowedHeaders("*")                        // 允许所有请求头
                .allowCredentials(true)                     // 允许携带 Cookie
                .maxAge(3600);                              // 预检请求缓存时间（秒）
    }
}