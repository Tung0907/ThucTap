package org.example.thuctap.Config;

import org.example.thuctap.Security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")   // bảo vệ api
                .excludePathPatterns("/api/auth/**", "/api/users", "/api/users/*") ; // public endpoints
    }

    // nếu bạn để static (index.html, login.html) trong /static thì mapping ok
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // mặc định Spring Boot phục vụ /static, /public
    }
}
