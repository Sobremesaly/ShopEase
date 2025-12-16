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
import com.shopease.utils.RedisUtil;
import com.shopease.vo.LoginVO;
import com.shopease.vo.UserInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author hspcadmin
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private RedisUtil redisUtil;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "shopease:refresh_token:";

    private static final String USER_REFRESH_TOKENS_KEY_PREFIX = "shopease:user:refresh_tokens:";

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        SysUser user = baseMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            log.error("登录失败，用户名不存在：{}", loginDTO.getUsername());
            throw new BusinessException("用户名不存在");
        }

        // 2. 校验密码
        boolean passwordMatch = PasswordUtils.matches(loginDTO.getPassword(), user.getPassword());
        if (!passwordMatch) {
            log.error("登录失败，密码错误，用户名：{}", loginDTO.getUsername());
            throw new BusinessException("密码错误");
        }

        // 3. 校验用户状态
        if (user.getStatus() == 0) {
            log.error("登录失败，账号已被禁用，用户名：{}", loginDTO.getUsername());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 4. 生成双Token
        String accessToken = JwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = JwtUtils.generateRefreshToken();
        Long userId = user.getId();

        // 5. 将Refresh Token存入Redis（双Key结构，处理异常）
        try {
            // 5.1 String类型：refreshToken -> userId（带过期时间）
            String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
            long expireSeconds = JwtUtils.getRefreshTokenExpiration() / 1000;
            redisUtil.set(refreshTokenKey, userId.toString(), expireSeconds, TimeUnit.SECONDS);
            log.debug("Refresh Token[String]存储成功，key：{}，过期时间：{}秒", refreshTokenKey, expireSeconds);

            // 5.2 Hash类型：userId -> refreshToken（用于批量删除）
            String userRefreshTokensKey = USER_REFRESH_TOKENS_KEY_PREFIX + userId;
            redisUtil.hset(userRefreshTokensKey, refreshToken, true);
            // 给Hash键也设置相同的过期时间（和String类型保持一致）
            redisUtil.expire(userRefreshTokensKey, expireSeconds, TimeUnit.SECONDS);
            log.debug("Refresh Token[Hash]存储成功，key：{}，refreshToken：{}", userRefreshTokensKey, refreshToken);
        } catch (Exception e) {
            log.error("用户{}登录时，Redis存储Refresh Token失败", loginDTO.getUsername(), e);
            throw new BusinessException("登录失败，请重试");
        }

        // 6. 封装响应数据
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(userId);
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        log.info("用户登录成功，用户名：{}", loginDTO.getUsername());
        return loginVO;
    }

    /**
     * 刷新Access Token接口
     */
    @Override
    public String refreshAccessToken(String refreshToken) {
        // 1. 校验refreshToken是否为空
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BusinessException("刷新令牌不能为空");
        }
        refreshToken = refreshToken.trim();

        // 2. 从Redis中查询refreshToken对应的用户ID（处理异常）
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userIdStr;
        try {
            userIdStr = redisUtil.get(refreshTokenKey);
        } catch (Exception e) {
            log.error("查询Refresh Token失败，refreshToken：{}", refreshToken, e);
            throw new BusinessException("刷新令牌验证失败，请重试");
        }

        // 3. 校验refreshToken是否有效
        if (userIdStr == null) {
            throw new BusinessException("刷新令牌已过期或无效，请重新登录");
        }

        // 4. 查询用户信息
        Long userId = Long.parseLong(userIdStr);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            // 清理无效的refreshToken（处理删除失败）
            cleanRefreshToken(userId, refreshToken);
            throw new BusinessException("用户不存在或已被禁用，请重新登录");
        }

        // 5. 生成新的Access Token
        String newAccessToken = JwtUtils.generateAccessToken(userId, user.getUsername());
        log.info("用户{}的Access Token已刷新", user.getUsername());
        return newAccessToken;
    }

    /**
     * 注册逻辑（无修改）
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        SysUser existUser = sysUserMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("username", registerDTO.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        String encryptedPassword = PasswordUtils.encrypt(registerDTO.getPassword());
        SysUser newUser = new SysUser();
        BeanUtils.copyProperties(registerDTO, newUser);
        newUser.setPassword(encryptedPassword);
        newUser.setStatus(1);
        sysUserMapper.insert(newUser);
    }

    /**
     * 查询当前登录用户信息（无修改）
     */
    @Override
    public UserInfoVO getCurrentUser() {
        Long currentUserId = JwtUtils.getUserIdFromToken();
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    /**
     * 修改当前登录用户信息（无修改）
     */
    @Override
    public void updateCurrentUser(UpdateUserDTO updateUserDTO) {
        Long currentUserId = JwtUtils.getUserIdFromToken();
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUserId);
        updateUser.setNickname(updateUserDTO.getNickname());
        updateUser.setPhone(updateUserDTO.getPhone());
        sysUserMapper.updateById(updateUser);
    }

    /**
     * 修改密码逻辑（批量删除用户的所有Refresh Token）
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

        // 5. 【关键】批量删除该用户的所有Refresh Token（处理删除失败）
        cleanAllRefreshTokens(currentUserId);
        log.info("用户{}修改密码成功，已清理所有Refresh Token", user.getUsername());
    }

    /**
     * 更新用户头像（无修改）
     */
    @Override
    public boolean updateAvatar(Long userId, String avatarUrl) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        return this.updateById(user);
    }

    /**
     * 退出登录接口（删除指定的Refresh Token）
     */
    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BusinessException("刷新令牌不能为空");
        }
        refreshToken = refreshToken.trim();

        // 1. 获取refreshToken对应的用户ID
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userIdStr = redisUtil.get(refreshTokenKey);
        if (userIdStr == null) {
            log.warn("退出登录时，Refresh Token已失效：{}", refreshToken);
            return;
        }
        Long userId = Long.parseLong(userIdStr);

        // 2. 清理该refreshToken
        cleanRefreshToken(userId, refreshToken);
        log.info("用户{}退出登录成功，Refresh Token已删除", userId);
    }

    // ==================== 私有工具方法（处理Refresh Token的清理） ====================

    /**
     * 清理单个Refresh Token（处理删除失败的情况）
     * @param userId 用户ID
     * @param refreshToken 刷新令牌
     */
    private void cleanRefreshToken(Long userId, String refreshToken) {
        // 定义要删除的Key
        String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        String userRefreshTokensKey = USER_REFRESH_TOKENS_KEY_PREFIX + userId;

        try {
            // 1. 删除String类型的refreshToken
            Boolean isStringDeleted = redisUtil.delete(refreshTokenKey);
            if (Boolean.TRUE.equals(isStringDeleted)) {
                log.debug("删除Refresh Token[String]成功，key：{}", refreshTokenKey);
            } else {
                log.warn("删除Refresh Token[String]失败，key：{}（可能已过期）", refreshTokenKey);
            }

            // 2. 删除Hash类型中的refreshToken
            Long hashDeleteCount = redisUtil.hdelete(userRefreshTokensKey, refreshToken);
            if (hashDeleteCount > 0) {
                log.debug("删除Refresh Token[Hash]成功，key：{}，refreshToken：{}", userRefreshTokensKey, refreshToken);
            } else {
                log.warn("删除Refresh Token[Hash]失败，key：{}，refreshToken：{}（可能已不存在）", userRefreshTokensKey, refreshToken);
            }
        } catch (Exception e) {
            log.error("清理Refresh Token失败，userId：{}，refreshToken：{}", userId, refreshToken, e);
            // 可选：抛出异常，让业务感知失败；或仅记录日志，不影响主流程
            // throw new BusinessException("退出登录失败，请重试");
        }
    }

    /**
     * 清理用户的所有Refresh Token（处理删除失败的情况）
     * @param userId 用户ID
     */
    private void cleanAllRefreshTokens(Long userId) {
        String userRefreshTokensKey = USER_REFRESH_TOKENS_KEY_PREFIX + userId;

        try {
            // 1. 获取该用户的所有Refresh Token
            Map<Object, Object> refreshTokenMap = redisUtil.hgetAll(userRefreshTokensKey);
            if (refreshTokenMap.isEmpty()) {
                log.debug("用户{}无Refresh Token需要清理", userId);
                return;
            }
            Set<Object> refreshTokens = refreshTokenMap.keySet();

            // 2. 遍历删除每个String类型的refreshToken
            for (Object tokenObj : refreshTokens) {
                String refreshToken = tokenObj.toString();
                String refreshTokenKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
                Boolean isDeleted = redisUtil.delete(refreshTokenKey);
                if (Boolean.FALSE.equals(isDeleted)) {
                    log.warn("删除用户{}的Refresh Token[String]失败，key：{}", userId, refreshTokenKey);
                }
            }

            // 3. 删除整个Hash键（批量删除用户的所有refreshToken）
            Boolean isHashDeleted = redisUtil.delete(userRefreshTokensKey);
            if (Boolean.TRUE.equals(isHashDeleted)) {
                log.debug("删除用户{}的Refresh Token[Hash]键成功，key：{}", userId, userRefreshTokensKey);
            } else {
                log.warn("删除用户{}的Refresh Token[Hash]键失败，key：{}", userId, userRefreshTokensKey);
            }
        } catch (Exception e) {
            log.error("清理用户{}的所有Refresh Token失败", userId, e);
            // 可选：抛出异常，阻止密码修改（保证安全）
            // throw new BusinessException("修改密码失败，请重试");
        }
    }

    public static void main(String[] args) {
        String encrypted = PasswordUtils.encrypt("123456");
        System.out.println(encrypted);
    }
}