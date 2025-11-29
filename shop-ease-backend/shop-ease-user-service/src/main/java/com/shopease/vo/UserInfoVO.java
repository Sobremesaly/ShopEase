package com.shopease.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息返回VO
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态（1正常，0禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}