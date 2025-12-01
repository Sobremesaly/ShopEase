package com.shopease.controller;

import com.shopease.dto.ChangePasswordDTO;
import com.shopease.dto.LoginDTO;
import com.shopease.dto.RegisterDTO;
import com.shopease.dto.UpdateUserDTO;
import com.shopease.result.Result;
import com.shopease.service.SysUserService;
import com.shopease.vo.LoginVO;
import com.shopease.vo.UserInfoVO;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 系统用户控制器
 * 负责用户登录、注册、个人信息管理等接口
 *
 * @author hspcadmin
 * &#064;date  2025-11-30
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
     * @return 登录结果（JWT令牌+用户基本信息）
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
}
