package com.shopease.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 自定义Redis连接属性类（读取shopease.redis前缀的配置）
 * @author hspcadmin
 */
@Data
@Component
@ConfigurationProperties(prefix = "shopease.redis")
public class RedisProperties {
    // 默认地址
    private String host = "localhost";
    // 默认端口
    private int port = 6379;
    // 默认密码
    private String password = "";
    // 默认数据库
    private int database = 0;
    // 连接池属性
    private Pool pool = new Pool();

    @Data
    public static class Pool {
        private int maxActive = 8;
        private int maxIdle = 8;
        private int minIdle = 2;
        private long maxWait = 1000;
    }
}
