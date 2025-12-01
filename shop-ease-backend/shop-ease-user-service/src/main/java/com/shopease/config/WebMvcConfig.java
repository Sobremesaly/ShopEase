package com.shopease.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

/**
 * Web配置（静态资源映射、跨域等）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LocalStorageConfig localStorageConfig;

    /**
     * 配置静态资源映射（让前端访问本地存储的文件）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 仅当本地存储启用时，添加映射
        if (localStorageConfig.isEnabled()) {
            // 前端访问路径前缀：/upload/**
            // 映射到本地目录：localStorageConfig.getRootDir()
            String resourcePattern = localStorageConfig.getAccessPrefix() + "**";
            String location = "file:" + localStorageConfig.getRootDir() + File.separator;

            // 添加映射（注意：location必须以 "file:" 开头，表示本地文件系统）
            registry.addResourceHandler(resourcePattern)
                    .addResourceLocations(location)
                    // 缓存1小时（优化性能）
                    .setCachePeriod(3600);
        }

        // 其他静态资源映射（如默认的 classpath:/static/）
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
