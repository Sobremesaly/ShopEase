package com.shopease.exception;

/**
 * 自定义业务异常类
 * 用于封装业务逻辑校验失败的异常（如密码错误、用户名已存在、权限不足等）
 * 继承 RuntimeException，无需强制捕获，适合业务场景抛出
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
public class BusinessException extends RuntimeException {

    /**
     * 构造方法：传入业务异常提示信息
     *
     * @param message 业务异常描述（会返回给前端）
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 构造方法：传入异常提示信息和根因异常
     * 适用于需要追溯异常链的场景（如业务异常由其他异常触发）
     *
     * @param message 业务异常描述
     * @param cause 根因异常（原始异常）
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}