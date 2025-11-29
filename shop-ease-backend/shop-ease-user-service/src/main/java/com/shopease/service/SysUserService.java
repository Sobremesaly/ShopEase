package com.shopease.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shopease.dto.LoginDTO;
import com.shopease.entity.SysUser;
import com.shopease.vo.LoginVO;


public interface SysUserService extends IService<SysUser> {
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录返回结果
     */
    LoginVO login(LoginDTO loginDTO);
}
