package de.hs_mannheim.informatik.ct.web;

import de.hs_mannheim.informatik.ct.controller.interceptor.CheckInInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckInInterceptor())
                .addPathPatterns("/")
                .addPathPatterns("/r/**");
    }
}