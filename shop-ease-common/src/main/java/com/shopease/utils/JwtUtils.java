package com.shopease.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * @author hspcadmin
 */
@Component
public class JwtUtils {

    private static String secret;
    private static long expiration;

    // 通过setter注入到静态字段
    @Value("${shopease.jwt.secret:shopease-secret-key-32bytes-long-12345678}")
    public void setSecret(String secret) {
        JwtUtils.secret = secret;
    }

    @Value("${shopease.jwt.expiration:7200000}")
    public void setExpiration(long expiration) {
        JwtUtils.expiration = expiration;
    }

    // 生成 Token
    public static String generateToken(Long userId, String username) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    // 验证 Token 有效性
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

    // 从 Token 中获取用户信息
    public static Claims getClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
