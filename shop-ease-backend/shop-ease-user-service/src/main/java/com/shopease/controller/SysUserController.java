package com.shopease.controller;

import com.shopease.dto.ChangePasswordDTO;
import com.shopease.dto.LoginDTO;
import com.shopease.dto.RegisterDTO;
import com.shopease.dto.UpdateUserDTO;
import com.shopease.result.Result;
import com.shopease.service.SysUserService;
import com.shopease.vo.LoginVO;
import com.shopease.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;

/**
 * 系统用户控制器
 * 负责用户登录、注册、个人信息管理等接口
 *
 * @author hspcadmin
 * @date  2025-11-30
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 构造器注入（无需@Autowired，Spring自动装配）
     *
     * @param sysUserService 用户服务接口
     */
    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 用户登录接口
     *
     * @param loginDTO 登录入参（用户名+密码）
     * @return 登录结果（JWT令牌+用户基本信息+刷新令牌）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户注册接口
     *
     * @param registerDTO 注册入参（用户名、密码、昵称、手机号）
     * @return 注册结果（成功提示）
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return Result.success("注册成功");
    }

    /**
     * 查询当前登录用户信息
     * 无需传入用户ID，从JWT Token中解析
     *
     * @return 当前用户信息（隐藏密码）
     */
    @GetMapping("/current")
    public Result<UserInfoVO> getCurrentUser() {
        UserInfoVO userInfoVO = sysUserService.getCurrentUser();
        return Result.success(userInfoVO);
    }

    /**
     * 修改当前登录用户信息
     *
     * @param updateUserDTO 修改入参（昵称、手机号，可选）
     * @return 修改结果（成功提示）
     */
    @PutMapping("/current")
    public Result<?> updateCurrentUser(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
        sysUserService.updateCurrentUser(updateUserDTO);
        return Result.success("修改成功");
    }

    /**
     * 修改登录密码
     *
     * @param changePasswordDTO 修改密码入参（原密码、新密码、确认新密码）
     * @return 修改结果（成功提示）
     */
    @PutMapping("/password")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        sysUserService.changePassword(changePasswordDTO);
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 刷新Access Token接口
     * 前端携带过期的accessToken时，用refreshToken获取新的accessToken
     * 刷新令牌（从请求体传入，也可根据需求改为请求参数/请求头）
     *
     * @return 新的Access Token
     */
    @PostMapping("/refreshToken")
    public Result<String> refreshAccessToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        String newAccessToken = sysUserService.refreshAccessToken(refreshTokenDTO.getRefreshToken());
        return Result.success(newAccessToken, "刷新令牌成功");
    }

    /**
     * 用户退出登录接口
     * 清理Redis中的refreshToken，使其无法再刷新accessToken
     * 刷新令牌（从请求体传入）
     *
     * @return 退出结果（成功提示）
     */
    @PostMapping("/logout")
    public Result<?> logout(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        sysUserService.logout(refreshTokenDTO.getRefreshToken());
        return Result.success("退出登录成功");
    }

    /**
     * 更新用户头像接口
     * 实际开发中，avatarUrl通常是文件上传后返回的OSS/本地存储URL
     *
     * @param userId     用户ID（也可从JWT解析，这里保留参数便于灵活调用）
     * @param avatarUrl  头像的访问URL
     * @return 更新结果（成功/失败）
     */
    @PutMapping("/avatar")
    public Result<?> updateAvatar(
            @RequestParam @Valid @NotBlank(message = "用户ID不能为空") Long userId,
            @RequestParam @Valid @NotBlank(message = "头像URL不能为空") String avatarUrl) {
        boolean isSuccess = sysUserService.updateAvatar(userId, avatarUrl);
        if (isSuccess) {
            return Result.success("头像更新成功");
        } else {
            return Result.error("头像更新失败");
        }
    }

    /**
     * 内部静态DTO：接收刷新令牌的入参（避免零散参数，符合RESTful规范）
     */
    public static class RefreshTokenDTO {
        @NotBlank(message = "刷新令牌不能为空")
        private String refreshToken;

        // Getter和Setter
        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}