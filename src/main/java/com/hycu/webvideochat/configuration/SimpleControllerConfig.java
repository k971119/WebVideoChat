package com.hycu.webvideochat.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 단순 연결 컨트롤러 설정
 */
@Configuration
public class SimpleControllerConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/ios").setViewName("chatRoom_ios");
    }
}
