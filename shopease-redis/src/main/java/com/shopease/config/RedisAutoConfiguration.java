package com.shopease.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * 自定义Redis自动配置类（包含连接配置+序列化配置）
 * @author hspcadmin
 */
@Configuration
public class RedisAutoConfiguration {

    @Resource
    private RedisProperties redisProperties;

    /**
     * 手动创建LettuceConnectionFactory（自定义连接配置）
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 1. 配置连接池
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(redisProperties.getPool().getMaxActive());
        poolConfig.setMaxIdle(redisProperties.getPool().getMaxIdle());
        poolConfig.setMinIdle(redisProperties.getPool().getMinIdle());
        poolConfig.setMaxWaitMillis(redisProperties.getPool().getMaxWait());

        // 2. 配置Lettuce客户端（带连接池）
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                // 命令超时时间
                .commandTimeout(Duration.ofSeconds(5))
                // 关闭超时时间
                .shutdownTimeout(Duration.ofSeconds(1))
                .build();

        // 3. 创建LettuceConnectionFactory（连接Redis）
        org.springframework.data.redis.connection.RedisStandaloneConfiguration redisConfig =
                new org.springframework.data.redis.connection.RedisStandaloneConfiguration();
        redisConfig.setHostName(redisProperties.getHost());
        redisConfig.setPort(redisProperties.getPort());
        redisConfig.setPassword(redisProperties.getPassword());
        redisConfig.setDatabase(redisProperties.getDatabase());

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    // 以下是原有序列化配置（不变）
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jacksonSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jacksonSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
