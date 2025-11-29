package com.shopease.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopease.dto.LoginDTO;
import com.shopease.entity.SysUser;
import com.shopease.mapper.SysUserMapper;
import com.shopease.service.SysUserService;
import com.shopease.utils.JwtUtils;
import com.shopease.utils.PasswordUtils;
import com.shopease.vo.LoginVO;
import org.springframework.stereotype.Service;

/**
 * @author hspcadmin
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        SysUser user = baseMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 2. 校验密码（明文密码 vs 数据库加密密码）
        boolean passwordMatch = PasswordUtils.matches(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatch) {
            throw new RuntimeException("密码错误");
        }

        // 3. 校验用户状态（是否禁用）
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 4. 生成 JWT Token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());

        // 5. 封装响应数据
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setToken(token);
        return loginVO;
    }
}
