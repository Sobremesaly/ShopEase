package com.shopease.exception;

import com.shopease.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获Controller层、Service层抛出的所有异常，返回标准化响应
 * 避免直接向客户端暴露异常堆栈信息，同时简化异常处理逻辑
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.shopease.controller")
public class GlobalExceptionHandler {

    /**
     * 捕获JSON解析异常
     * 适用于前端传入的JSON格式不合法的场景（如字段名未加双引号、语法错误）
     *
     * @param e 具体解析异常对象
     * @return 标准化失败响应，包含错误提示
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleJsonParseException(HttpMessageNotReadableException e) {
        log.error("JSON解析失败", e);
        return Result.error("请求参数格式错误，请传入标准JSON");
    }

    /**
     * 捕获媒体类型不支持异常
     * 适用于前端未设置Content-Type或设置错误的场景（如需要application/json却传form-data）
     *
     * @param e 具体的媒体类型异常对象
     * @return 标准化失败响应，包含错误提示
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> handleMediaTypeException(HttpMediaTypeNotSupportedException e) {
        log.error("媒体类型不支持", e);
        return Result.error("请求格式不支持，请用JSON格式并设置Content-Type: application/json");
    }

    /**
     * 捕获自定义业务异常（优先级高于RuntimeException）
     * 适用于业务逻辑校验失败的场景（如密码错误、用户名已存在、原密码错误等）
     *
     * @param e 具体的业务异常对象（手动抛出的BusinessException）
     * @return 标准化失败响应，返回业务异常描述信息
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        // 业务异常无需打印完整堆栈（已知原因）
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 捕获通用运行时异常（如空指针、数组越界等）
     * 适用于非业务逻辑的运行时错误，需打印完整堆栈便于排查
     *
     * @param e 具体的运行时异常对象
     * @return 标准化失败响应，隐藏具体错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleRuntimeException(RuntimeException e) {
        // 打印完整堆栈，便于排查未知错误
        log.error("运行时异常", e);
        return Result.error(e.getMessage());
    }

    /**
     * 捕获系统异常（所有未定义的异常）
     * 作为兜底异常处理，适用于系统内部错误（如数据库连接失败、代码bug）
     *
     * @param e 具体的系统异常对象
     * @return 标准化失败响应，隐藏具体错误信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleAllException(Exception e) {
        // 打印完整堆栈，便于排查
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后再试");
    }
}