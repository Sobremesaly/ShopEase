package com.shopease.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * 修改个人信息入参DTO
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Data
public class UpdateUserDTO {

    /**
     * 昵称（可选）
     */
    @Size(max = 50, message = "昵称长度不能超过50位")
    private String nickname;

    /**
     * 手机号（可选）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 头像（可选）
     * 校验规则：URL格式（正则）
     */
    @Pattern(regexp = "^(https?://).+", message = "头像URL格式不正确")
    private String avatar;
}
