package com.shopease.vo;

import lombok.Data;

/**
 * @author hspcadmin
 */
@Data
public class LoginVO {

    // 用户ID
    private Long userId;

    // 用户名
    private String username;

    // 昵称
    private String nickname;

    // 生成的Token（核心）
    private String token;

    //头像
    private String avatar;
}
