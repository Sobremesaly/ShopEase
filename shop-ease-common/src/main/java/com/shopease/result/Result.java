package com.shopease.result;// shop-ease-common/src/main/java/com/shopease/common/result/Result.java
import com.shopease.constans.Constant;
import lombok.Data;

import static com.shopease.constans.Constant.SUCCESS_CODE;

/**
 * @author hspcadmin
 */
@Data
public class Result<T> {

    // 响应码：200成功，500失败，401未登录
    private int code;

    // 响应信息
    private String msg;

    // 响应数据
    private T data;

    public Result(int i, String s, T data) {
    }

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(Constant.SUCCESS_CODE, Constant.SUCCESS_MESSAGE, data);
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return new Result<>(Constant.SUCCESS_CODE, Constant.SUCCESS_MESSAGE, null);
    }

    // 失败响应
    public static <T> Result<T> error(String msg) {
        return new Result<>(Constant.FAIL_CODE, msg, null);
    }

    // 未登录响应
    public static <T> Result<T> unAuth() {
        return new Result<>(Constant.UNAUTH_CODE, Constant.UNAUTH_MESSAGE, null);
    }
}
