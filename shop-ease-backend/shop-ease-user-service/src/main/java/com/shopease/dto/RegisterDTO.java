package com.shopease.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

/**
 * 用户注册入参DTO
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Data
public class RegisterDTO {

    /**
     * 用户名（登录用）
     * 校验规则：非空、长度2-20位、仅包含字母/数字/下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名仅支持字母、数字、下划线")
    private String username;

    /**
     * 密码
     * 校验规则：非空、长度6-20位、包含字母+数字（增强安全性）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    /**
     * 确认密码
     * 校验规则：非空、与密码一致（通过自定义注解实现，下文提供）
     */
    @NotBlank(message = "请确认密码")
    private String confirmPassword;

    /**
     * 昵称（可选）
     * 校验规则：长度0-50位
     */
    @Size(max = 50, message = "昵称长度不能超过50位")
    private String nickname;

    /**
     * 手机号（可选）
     * 校验规则：符合手机号格式（正则）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}