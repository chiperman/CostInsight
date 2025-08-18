package com.costinsight.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 JWT 拦截器，并指定需要拦截的路径和排除的路径
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**") // 拦截所有 /api/ 开头的请求
                .excludePathPatterns("/api/auth/**"); // 排除 /api/auth/ 开头的请求（如登录、注册）
    }
}