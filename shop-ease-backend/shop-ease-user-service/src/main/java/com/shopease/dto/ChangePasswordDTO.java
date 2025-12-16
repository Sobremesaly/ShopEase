package com.shopease.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.shopease.validator.PasswordMatch;

/**
 * 修改密码入参DTO
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Data
@PasswordMatch(message = "两次输入的新密码不一致")
public class ChangePasswordDTO {

    /**
     * 原密码
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码（同注册密码规则）
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "新密码必须包含字母和数字")
    private String newPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "请确认新密码")
    private String confirmNewPassword;
}