// 创建新文件 WebConfig.java
package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // 配置静态资源访问
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射图片上传路径，使上传的图片可以通过HTTP访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}