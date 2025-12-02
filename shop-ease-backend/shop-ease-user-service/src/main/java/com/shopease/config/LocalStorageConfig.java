package com.shopease.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 本地存储配置类（绑定 application.yml 中的 local-storage 配置）
 * @author hspcadmin
 */
@Data
@Component
@ConfigurationProperties(prefix = "local-storage") // 对应配置文件的前缀
public class LocalStorageConfig {

    // 是否启用本地存储
    private boolean enabled;

    // 存储根目录
    private String rootDir;

    // 前端访问前缀
    private String accessPrefix;

    // 最大文件大小（字节）
    private long maxFileSize;

    // 允许的文件类型
    private List<String> allowTypes;
}
