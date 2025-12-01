package com.shopease.controller;

import com.shopease.result.Result;
import com.shopease.service.SysUserService;
import com.shopease.utils.JwtUtils;
import com.shopease.utils.LocalStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户头像上传接口
 */
@RestController
@RequestMapping("/sys/user")
public class UserAvatarController {

    @Autowired
    private LocalStorageUtil localStorageUtil;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        // 1. 上传文件到本地（模块名设为 "avatar"，区分其他文件）
        String avatarUrl = localStorageUtil.upload(file, "avatar");

        // 2. 更新当前登录用户的 avatar 字段
        Long userId = JwtUtils.getUserIdFromToken();
        sysUserService.updateAvatar(userId, avatarUrl);

        // 3. 返回前端可访问的 URL
        return Result.success(avatarUrl, "头像上传成功");
    }
}
