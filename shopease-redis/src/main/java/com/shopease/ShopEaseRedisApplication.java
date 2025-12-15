package com.shopease;

import com.shopease.utils.RedisUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author hspcadmin
 */
@SpringBootApplication
public class ShopEaseRedisApplication {

    @Resource
    private RedisUtil redisUtil;

    public static void main(String[] args) {
        SpringApplication.run(ShopEaseRedisApplication.class, args);
    }

    @Bean
    public CommandLineRunner testRedis() {
        return args -> {
            System.out.println("========== 开始测试Redis工具类 ==========");

            // 1. 测试String类型操作
            testStringOperations();

            // 2. 测试Hash类型操作
            testHashOperations();

            // 3. 测试List类型操作
            testListOperations();

            // 4. 测试Set类型操作
            testSetOperations();

            // 5. 测试ZSet类型操作
            testZSetOperations();

            // 6. 测试过期时间
            testExpireOperations();

            System.out.println("========== Redis工具类测试完成 ==========");
        };
    }

    private void testStringOperations() {
        System.out.println("\n--- 测试String类型操作 ---");

        // 测试set和get
        String key = "test:string:user:1001";
        String value = "{\"id\":1001,\"name\":\"张三\",\"age\":25}";

        redisUtil.set(key, value);
        System.out.println("设置值: " + value);

        String result = redisUtil.get(key);
        System.out.println("获取值: " + result);

        // 测试带过期时间的set
        String tempKey = "test:string:temp:data";
        redisUtil.set(tempKey, "临时数据", 10, TimeUnit.SECONDS);
        System.out.println("设置带过期时间的键: " + tempKey);

        // 测试删除
        redisUtil.delete(key);
        System.out.println("删除键: " + key);

        String deletedValue = redisUtil.get(key);
        System.out.println("删除后获取(应为null): " + deletedValue);
    }

    private void testHashOperations() {
        System.out.println("\n--- 测试Hash类型操作 ---");

        String hashKey = "test:hash:product:2001";

        // 设置hash字段
        redisUtil.hset(hashKey, "name", "iPhone 15");
        redisUtil.hset(hashKey, "price", "6999");
        redisUtil.hset(hashKey, "stock", "100");
        redisUtil.hset(hashKey, "category", "手机");

        System.out.println("设置Hash字段完成");

        // 获取单个字段
        Object productName = redisUtil.hget(hashKey, "name");
        System.out.println("获取name字段: " + productName);

        // 获取整个hash
        Map<Object, Object> productInfo = redisUtil.hgetAll(hashKey);
        System.out.println("获取整个Hash:");
        productInfo.forEach((field, val) ->
                System.out.println("  " + field + ": " + val));

        // 删除hash中的某个字段
        redisUtil.hdelete(hashKey, "stock");
        System.out.println("删除stock字段后:");
        Map<Object, Object> afterDelete = redisUtil.hgetAll(hashKey);
        afterDelete.forEach((field, val) ->
                System.out.println("  " + field + ": " + val));

        // 清理测试数据
        redisUtil.delete(hashKey);
    }

    private void testListOperations() {
        System.out.println("\n--- 测试List类型操作 ---");

        String listKey = "test:list:messages";

        // 向列表尾部添加元素
        redisUtil.lpush(listKey, "第一条消息");
        redisUtil.lpush(listKey, "第二条消息");
        redisUtil.lpush(listKey, "第三条消息");

        System.out.println("向列表添加3条消息");

        // 获取列表所有元素
        List<Object> messages = redisUtil.lrange(listKey, 0, -1);
        System.out.println("获取列表所有消息:");
        for (int i = 0; i < messages.size(); i++) {
            System.out.println("  消息" + (i + 1) + ": " + messages.get(i));
        }

        // 获取部分元素
        List<Object> firstTwo = redisUtil.lrange(listKey, 0, 1);
        System.out.println("获取前两条消息:");
        firstTwo.forEach(msg -> System.out.println("  " + msg));

        // 清理测试数据
        redisUtil.delete(listKey);
    }

    private void testSetOperations() {
        System.out.println("\n--- 测试Set类型操作 ---");

        String setKey = "test:set:tags";

        // 向Set添加元素
        // 重复元素会自动去重
        redisUtil.sadd(setKey, "热门", "推荐", "新品", "热销", "推荐");

        System.out.println("向Set添加标签(包含重复标签)");

        // 获取Set所有元素
        Set<Object> tags = redisUtil.smembers(setKey);
        System.out.println("获取Set所有标签(自动去重):");
        tags.forEach(tag -> System.out.println("  " + tag));

        // 清理测试数据
        redisUtil.delete(setKey);
    }

    private void testZSetOperations() {
        System.out.println("\n--- 测试ZSet类型操作 ---");

        String zsetKey = "test:zset:ranking";

        // 向ZSet添加元素（带分数）
        redisUtil.zadd(zsetKey, "用户A", 100.0);
        redisUtil.zadd(zsetKey, "用户B", 85.5);
        redisUtil.zadd(zsetKey, "用户C", 92.0);
        redisUtil.zadd(zsetKey, "用户D", 78.0);

        System.out.println("向ZSet添加4个用户的分数");

        // 获取指定分数范围的元素
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> ranking =
                redisUtil.zrangeByScore(zsetKey, 80.0, 100.0);

        System.out.println("获取80-100分的用户:");
        ranking.forEach(tuple -> System.out.println("  用户: " + tuple.getValue() + ", 分数: " + tuple.getScore()));

        // 清理测试数据
        redisUtil.delete(zsetKey);
    }

    private void testExpireOperations() {
        System.out.println("\n--- 测试过期时间操作 ---");

        String expireKey = "test:expire:data";

        // 设置值并添加过期时间
        redisUtil.set(expireKey, "这个数据10秒后过期");
        redisUtil.expire(expireKey, 10, TimeUnit.SECONDS);

        System.out.println("设置键 " + expireKey + " 的过期时间为10秒");

        // 检查键是否存在（通过获取值）
        String value = redisUtil.get(expireKey);
        System.out.println("立即获取值: " + value);

        System.out.println("注意：这个键会在10秒后自动过期");
    }
}