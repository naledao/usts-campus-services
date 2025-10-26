package hhsc.kangnasi.xyz.ustscampusservices.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // auth endpoints
                        "/user/send-login-code",
                        "/user/login",
                        "/user/validate-token",
                        // health/error/static
                        "/error",
                        "/favicon.ico",
                        "/service-dorm-electricity-alert-room/init-rooms",
                        "/swagger-ui/**",
                        "/v3/**",
                        "/service-dorm-electricity-alert/**"
                );
    }
}

