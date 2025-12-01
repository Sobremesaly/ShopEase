package com.shopease.result;

import com.shopease.constans.Constant;
import lombok.Data;

/**
 * 全局统一响应结果类
 * 所有接口的响应都通过此类封装，确保前端接收格式一致
 *
 * @param <T> 响应数据的泛型类型（支持任意数据结构）
 * @author hspcadmin
 * @date 2025-11-30
 */
@Data
public class Result<T> {

    /**
     * 响应码：参考 {@link Constant} 中的常量定义
     * 200=成功，500=业务失败，401=未登录
     */
    private int code;

    /**
     * 响应信息：操作结果的描述文本（成功/失败原因）
     */
    private String msg;

    /**
     * 响应数据：业务返回的具体数据（成功时返回，失败时可为null）
     */
    private T data;

    /**
     * 带参构造方法：初始化响应码、响应信息、响应数据
     *
     * @param code 响应码
     * @param msg 响应信息
     * @param data 响应数据
     */
    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 业务返回的具体数据
     * @param <T> 数据泛型类型
     * @param message 自定义成功提示信息
     * @return 封装后的成功响应对象
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(Constant.SUCCESS_CODE, message, data);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 业务返回的具体数据
     * @param <T> 数据泛型类型
     * @return 封装后的成功响应对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Constant.SUCCESS_CODE, Constant.SUCCESS_MESSAGE, data);
    }

    /**
     * 成功响应（无数据）
     * 适用于不需要返回业务数据的操作（如删除、更新）
     *
     * @param <T> 泛型占位符（无实际数据时使用）
     * @return 封装后的成功响应对象
     */
    public static <T> Result<T> success() {
        return new Result<>(Constant.SUCCESS_CODE, Constant.SUCCESS_MESSAGE, null);
    }

    /**
     * 失败响应（自定义提示信息）
     * 适用于业务逻辑校验失败的场景（如密码错误、用户名不存在）
     *
     * @param msg 失败原因描述
     * @param <T> 泛型占位符（无实际数据时使用）
     * @return 封装后的失败响应对象
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(Constant.FAIL_CODE, msg, null);
    }

    /**
     * 未登录响应
     * 适用于用户未登录或Token失效时的场景
     *
     * @param <T> 泛型占位符（无实际数据时使用）
     * @return 封装后的未登录响应对象
     */
    public static <T> Result<T> unAuth() {
        return new Result<>(Constant.UNAUTH_CODE, Constant.UNAUTH_MESSAGE, null);
    }
}
