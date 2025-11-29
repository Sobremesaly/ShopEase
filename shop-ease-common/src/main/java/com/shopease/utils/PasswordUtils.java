package com.shopease.utils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author hspcadmin
 */ // 密码加密工具（Spring Security提供，安全可靠）
public class PasswordUtils {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    // 密码加密（存储到数据库的是加密后的字符串）
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    // 密码校验（前端传的明文密码 vs 数据库加密后的密码）
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
