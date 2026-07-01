package com.group1.recruitment.config;

import com.group1.recruitment.security.AuthInterceptor;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final long MB = 1024L * 1024L;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
    }

    /**
     * Allow CV uploads up to 5 MB (SCR-14), overriding the 1 MB servlet default.
     * Uses the standard jakarta.servlet constructor:
     * (location, maxFileSize, maxRequestSize, fileSizeThreshold) in bytes.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(null, 5 * MB, 6 * MB, 0);
    }
}
