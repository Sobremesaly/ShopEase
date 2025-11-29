package com.shopease.validator;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 密码一致性校验逻辑实现
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordField;
    private String confirmPasswordField;

    /**
     * 初始化：获取注解中的字段名
     */
    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }

    /**
     * 校验逻辑：获取两个字段的值，对比是否一致
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(value);
        // 获取密码和确认密码的值
        Object password = beanWrapper.getPropertyValue(passwordField);
        Object confirmPassword = beanWrapper.getPropertyValue(confirmPasswordField);

        // 都为null时，由@NotBlank校验，此处返回true
        if (password == null && confirmPassword == null) {
            return true;
        }
        // 任一为null，返回false（@NotBlank会补充校验非空）
        if (password == null || confirmPassword == null) {
            return false;
        }
        // 对比两个值是否一致
        return password.equals(confirmPassword);
    }
}