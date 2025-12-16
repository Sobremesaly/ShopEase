package com.shopease.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.shopease.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类（双Token机制：Access Token + Refresh Token）
 * <p>
 * 功能包括：生成Access Token、生成Refresh Token、验证Token有效性、解析Token中的用户信息、
 * 从请求上下文获取当前登录用户ID等，适配JWT标准Base64URL编码，避免解码冲突。
 *
 * @author hspcadmin
 * &#064;date  2025-11-30
 * @since 1.0.0
 */
@Component
public class JwtUtils {

    /**
     * JWT签名密钥（从配置文件注入，默认值为32字节的密钥，满足HS256算法要求）
     */
    private static String secret;

    /**
     * Access Token过期时间（毫秒，默认30分钟：1800000ms，配置文件可自定义）
     */
    private static long accessTokenExpiration;

    /**
     * Refresh Token过期时间（毫秒，默认7天：604800000ms，配置文件可自定义）
     */
    @Getter
    private static long refreshTokenExpiration;

    /**
     * 注入JWT签名密钥到静态字段
     * <p>
     * Spring不支持直接注入静态字段，通过非静态setter方法间接注入
     *
     * @param secret 配置文件中的JWT签名密钥
     */
    @Value("${shopease.jwt.secret:shopease-secret-key-32bytes-long-12345678}")
    public void setSecret(String secret) {
        JwtUtils.secret = secret;
    }

    /**
     * 注入Access Token过期时间到静态字段
     *
     * @param expiration 配置文件中的Access Token过期时间（毫秒）
     */
    @Value("${shopease.jwt.access-token-expiration:1800000}")
    public void setExpiration(long expiration) {
        JwtUtils.accessTokenExpiration = expiration;
    }

    /**
     * 注入Refresh Token过期时间到静态字段
     *
     * @param expiration 配置文件中的Refresh Token过期时间（毫秒）
     */
    @Value("${shopease.jwt.refresh-token-expiration:604800000}")
    public void setRefreshTokenExpiration(long expiration) {
        JwtUtils.refreshTokenExpiration = expiration;
    }

    /**
     * 生成Refresh Token（采用UUID生成，简单安全，无加密，存储在服务端或客户端）
     * <p>
     * Refresh Token用于在Access Token过期后，获取新的Access Token，无需用户重新登录
     *
     * @return 去除横线的UUID字符串作为Refresh Token
     */
    public static String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成Access Token（包含用户ID和用户名，使用HS256算法签名）
     * <p>
     * Access Token用于接口鉴权，有效期较短，避免被盗用后造成长时间风险
     *
     * @param userId   用户ID（自定义Claim）
     * @param username 用户名（自定义Claim）
     * @return 签名后的JWT字符串（Access Token）
     */
    public static String generateAccessToken(Long userId, String username) {
        // 生成HS256算法所需的密钥（基于配置的secret）
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                // 自定义载荷：用户ID
                .claim("userId", userId)
                // 自定义载荷：用户名
                .claim("username", username)
                // 设置过期时间
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                // 使用HS256算法签名
                .signWith(SignatureAlgorithm.HS256, key)
                // 生成最终的Token字符串
                .compact();
    }

    /**
     * 验证Token的有效性（包括签名是否正确、是否过期）
     *
     * @param token 待验证的JWT字符串（Access Token）
     * @return true：Token有效；false：Token无效（签名错误、过期、格式错误等）
     */
    public static boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            // 解析Token并验证签名
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 捕获所有异常（签名异常、过期异常、解析异常等），返回false
            return false;
        }
    }

    /**
     * 从Token中解析出所有的Claims（载荷信息）
     *
     * @param token 待解析的JWT字符串（Access Token）
     * @return Token中的载荷信息（包含自定义Claim和标准Claim）
     */
    public static Claims getClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody(); // 获取载荷体
    }

    /**
     * 从HTTP请求的Authorization头中解析当前登录用户的ID（核心方法）
     * <p>
     * 步骤：1. 获取请求上下文；2. 提取Authorization头中的Token；3. 解析Token获取用户ID；
     * 兼容前端传递的Token格式（支持Bearer前缀，忽略大小写）
     *
     * @return 当前登录用户的ID（Long类型）
     * @throws BusinessException 以下情况会抛出业务异常：
     *                           1. 无法获取请求上下文（非HTTP请求调用）；
     *                           2. 未携带Authorization Token；
     *                           3. Token中未包含用户ID；
     *                           4. Token无效（过期、签名错误、格式错误等）；
     */
    public static Long getUserIdFromToken() {
        // 1. 校验请求上下文是否存在（避免空指针异常）
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new BusinessException("无法获取请求上下文，请确保接口通过HTTP请求访问");
        }

        // 2. 获取HTTP请求对象，并提取Authorization头中的Token
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("Authorization");
        // 判空处理（避免空指针）
        if (token == null) {
            throw new BusinessException("请先登录（未携带Authorization Token）");
        }
        // 去除首尾空格（避免前端传参带空格）
        token = token.trim();
        // 兼容Bearer前缀（忽略大小写，如：Bearer xxxx 或 bearer xxxx）
        if (token.toLowerCase().startsWith("bearer ")) {
            // 截取第7位之后的内容（"Bearer " 共7个字符：B e a r e r 空格）
            token = token.substring(7).trim();
        }
        // 校验Token是否为空字符串
        if (token.isEmpty()) {
            throw new BusinessException("请先登录（未携带Authorization Token）");
        }

        try {
            // 3. 解析Token获取载荷信息
            Claims claims = getClaims(token);
            // 4. 提取用户ID并校验（避免null）
            Long userId = claims.get("userId", Long.class);
            if (userId == null) {
                throw new BusinessException("Token中未包含用户ID");
            }
            return userId;
        } catch (BusinessException e) {
            // 捕获自定义业务异常，直接抛出（保留原始异常信息）
            throw e;
        } catch (Exception e) {
            // 捕获Token过期、签名错误、解析异常等，统一抛出登录过期异常
            throw new BusinessException("登录已过期，请重新登录");
        }
    }
}