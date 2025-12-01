package com.shopease.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopease.dto.ChangePasswordDTO;
import com.shopease.dto.LoginDTO;
import com.shopease.dto.RegisterDTO;
import com.shopease.dto.UpdateUserDTO;
import com.shopease.entity.SysUser;
import com.shopease.exception.BusinessException;
import com.shopease.mapper.SysUserMapper;
import com.shopease.service.SysUserService;
import com.shopease.utils.JwtUtils;
import com.shopease.utils.PasswordUtils;
import com.shopease.vo.LoginVO;
import com.shopease.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hspcadmin
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        SysUser user = baseMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            log.error("登录失败，用户名不存在：{}", loginDTO.getUsername());
            throw new RuntimeException("用户名不存在");
        }

        // 2. 校验密码（明文密码 vs 数据库加密密码）
        boolean passwordMatch = PasswordUtils.matches(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatch) {
            log.error("登录失败，密码错误，用户名：{}", loginDTO.getUsername());
            throw new RuntimeException("密码错误");
        }

        // 3. 校验用户状态（是否禁用）
        if (user.getStatus() == 0) {
            log.error("登录失败，账号已被禁用，用户名：{}", loginDTO.getUsername());
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
        log.info("用户登录成功，用户名：{}", loginDTO.getUsername());
        return loginVO;
    }

    /**
     * 注册逻辑
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        // 1. 校验用户名唯一性
        SysUser existUser = sysUserMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("username", registerDTO.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        // 2. 加密密码
        String encryptedPassword = PasswordUtils.encrypt(registerDTO.getPassword());
        // 3. 封装用户实体
        SysUser newUser = new SysUser();
        BeanUtils.copyProperties(registerDTO, newUser);
        // 存入加密后的密码
        newUser.setPassword(encryptedPassword);
        // 默认正常状态
        newUser.setStatus(1);
        // 4. 插入数据库
        sysUserMapper.insert(newUser);
    }

    /**
     * 查询当前登录用户信息
     */
    @Override
    public UserInfoVO getCurrentUser() {
        // 1. 从JWT Token中解析当前用户ID（需确保JwtUtils有解析方法）
        Long currentUserId = JwtUtils.getUserIdFromToken();
        // 2. 查询用户信息
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }
        // 3. 封装VO（隐藏密码）
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    /**
     * 修改当前登录用户信息
     */
    @Override
    public void updateCurrentUser(UpdateUserDTO updateUserDTO) {
        // 1. 从JWT解析当前用户ID
        Long currentUserId = JwtUtils.getUserIdFromToken();
        // 2. 封装更新实体
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUserId);
        updateUser.setNickname(updateUserDTO.getNickname());
        updateUser.setPhone(updateUserDTO.getPhone());
        // 3. 更新数据库
        sysUserMapper.updateById(updateUser);
    }

    /**
     * 修改密码逻辑
     */
    @Override
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        // 1. 从JWT解析当前用户ID
        Long currentUserId = JwtUtils.getUserIdFromToken();
        // 2. 查询当前用户
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }
        // 3. 验证原密码
        boolean oldPasswordMatch = PasswordUtils.matches(changePasswordDTO.getOldPassword(), user.getPassword());
        if (!oldPasswordMatch) {
            throw new BusinessException("原密码错误");
        }
        // 4. 加密新密码并更新
        String newEncryptedPassword = PasswordUtils.encrypt(changePasswordDTO.getNewPassword());
        user.setPassword(newEncryptedPassword);
        sysUserMapper.updateById(user);
    }

    /**
     * 更新用户头像
     */
    @Override
    public boolean updateAvatar(Long userId, String avatarUrl) {
        SysUser user = new SysUser();
        user.setId(userId);
        // 存入配置化目录生成的URL
        user.setAvatar(avatarUrl);
        return this.updateById(user);
    }

    public static void main(String[] args) {
        String encrypted = PasswordUtils.encrypt("123456");
        System.out.println(encrypted);
    }
}
