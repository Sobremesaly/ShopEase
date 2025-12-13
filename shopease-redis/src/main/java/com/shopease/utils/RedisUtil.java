package com.shopease.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类：封装常用的Redis操作，简化开发
 */
@Component // 注入到Spring容器
@RequiredArgsConstructor // Lombok：自动生成构造器，注入依赖（避免@Autowired）
public class RedisUtil {

    // 注入自定义的RedisTemplate和StringRedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // ==================== String类型操作（最常用） ====================
    /**
     * 存储字符串（带过期时间）
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 存储字符串（永久有效）
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取字符串值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除单个键
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除键
     */
    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    /**
     * 设置键的过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    // ==================== Hash类型操作 ====================
    /**
     * 存储Hash数据
     */
    public void hset(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取Hash中的指定值
     */
    public Object hget(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 获取整个Hash数据
     */
    public Map<Object, Object> hgetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Hash中的指定字段
     */
    public Long hdelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    // ==================== List类型操作 ====================
    /**
     * 向List尾部添加元素
     */
    public Long lpush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 获取List指定范围的元素
     */
    public List<Object> lrange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ==================== Set类型操作 ====================
    /**
     * 向Set中添加元素
     */
    public Long sadd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取Set中的所有元素
     */
    public Set<Object> smembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // ==================== ZSet（有序集合）类型操作 ====================
    /**
     * 向ZSet中添加元素（带分数）
     */
    public Boolean zadd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取ZSet中指定分数范围的元素（带分数）
     */
    public Set<ZSetOperations.TypedTuple<Object>> zrangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }
}
