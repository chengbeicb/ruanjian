// 创建新文件 WebConfig.java
package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到项目的 uploads/ 目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        
        // 新增：映射 /images/** 到 D:/project/rjgcsj/picture/ 目录
        // 这样，URL http://localhost:8080/images/chenshan.jpg 就会访问 D:/project/rjgcsj/picture/chenshan.jpg
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/project/rjgcsj/picture/")
                .setCachePeriod(0); // 禁用缓存以便调试
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 为所有路径添加CORS支持，包括静态资源
        // 使用 allowedOriginPatterns 替代 allowedOrigins 以支持 allowCredentials
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}