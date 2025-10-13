package org.example.thuctap.Config;

import org.example.thuctap.Security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**") // Áp dụng cho tất cả API
                .excludePathPatterns(
                        "/api/auth/**",   // bỏ qua đăng ký, đăng nhập
                        "/api/users/**",  // bỏ qua tạo user
                        "/login.html",
                        "/register.html"
                );
    }
}
