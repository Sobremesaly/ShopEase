package com.shopease.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 自定义校验注解：校验两次密码一致
 *
 * @author hspcadmin
 * &#064;date  2025-11-30
 */
@Target({ElementType.TYPE}) // 作用于类（因为要对比两个字段）
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class) // 校验逻辑实现类
@Documented
public @interface PasswordMatch {

    /**
     * 错误提示信息
     */
    String message() default "两次输入的密码不一致";

    /**
     * 分组校验（默认空）
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息（默认空）
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 密码字段名（默认password）
     */
    String passwordField() default "password";

    /**
     * 确认密码字段名（默认confirmPassword）
     */
    String confirmPasswordField() default "confirmPassword";
}