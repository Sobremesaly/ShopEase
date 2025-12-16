package com.shopease.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * @author hspcadmin
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
