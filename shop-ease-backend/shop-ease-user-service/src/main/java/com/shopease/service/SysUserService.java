package com.shopease.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopease.dto.ChangePasswordDTO;
import com.shopease.dto.LoginDTO;
import com.shopease.dto.RegisterDTO;
import com.shopease.dto.UpdateUserDTO;
import com.shopease.entity.SysUser;
import com.shopease.vo.LoginVO;
import com.shopease.vo.UserInfoVO;

/**
 * 系统用户服务接口
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     *
     * @param loginDTO 登录入参
     * @return 登录结果VO
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册入参
     */
    void register(RegisterDTO registerDTO);

    /**
     * 查询当前登录用户信息
     *
     * @return 用户信息VO
     */
    UserInfoVO getCurrentUser();

    /**
     * 修改当前登录用户信息
     *
     * @param updateUserDTO 修改入参
     */
    void updateCurrentUser(UpdateUserDTO updateUserDTO);

    /**
     * 修改登录密码
     *
     * @param changePasswordDTO 修改密码入参
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
}