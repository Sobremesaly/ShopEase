package com.shopease.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.shopease.exception.BusinessException; // 必须导入自定义业务异常
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 * 用于生成 Token、验证 Token 有效性、解析 Token 中的用户信息
 * 适配 JWT 标准 Base64URL 编码，避免解码冲突
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Component
public class JwtUtils {

    private static String secret;
    private static long expiration;

    // 通过setter注入到静态字段（无需修改，保持原有逻辑）
    @Value("${shopease.jwt.secret:shopease-secret-key-32bytes-long-12345678}")
    public void setSecret(String secret) {
        JwtUtils.secret = secret;
    }

    @Value("${shopease.jwt.expiration:7200000}")
    public void setExpiration(long expiration) {
        JwtUtils.expiration = expiration;
    }

    // 生成 Token（无需修改，已正确使用 Keys.hmacShaKeyFor）
    public static String generateToken(Long userId, String username) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    // 验证 Token 有效性（无需修改，已正确使用 parserBuilder）
    public static boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 从 Token 中获取用户信息（无需修改，已正确使用 parserBuilder）
    public static Claims getClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中解析用户ID（核心修复方法）
     *
     * @return 当前登录用户ID
     * @throws BusinessException 未携带Token、Token无效、Token过期时抛出
     */
    public static Long getUserIdFromToken() {
        // 1. 校验请求上下文是否存在（避免空指针）
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new BusinessException("无法获取请求上下文，请确保接口通过HTTP请求访问");
        }

        // 2. 获取请求头中的Token
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("请先登录（未携带Authorization Token）");
        }

        try {
            // 3. 复用已有的 getClaims 方法（避免重复代码，确保解析逻辑一致）
            Claims claims = getClaims(token);
            // 4. 解析用户ID（若不存在会返回null，需校验）
            Long userId = claims.get("userId", Long.class);
            if (userId == null) {
                throw new BusinessException("Token中未包含用户ID");
            }
            return userId;
        } catch (BusinessException e) {
            // 捕获自定义业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 捕获Token过期、签名错误等异常
            throw new BusinessException("登录已过期，请重新登录");
        }
    }
}