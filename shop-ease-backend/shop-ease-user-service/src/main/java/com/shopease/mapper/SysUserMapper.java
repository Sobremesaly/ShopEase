package com.shopease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopease.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户数据访问接口
 * 提供用户相关的数据库操作方法，继承 MyBatis-Plus BaseMapper，内置常用 CRUD 功能
 * 关联实体类：{@link com.shopease.entity.SysUser}
 *
 * @author hspcadmin
 * @date 2025-11-30
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户信息
     * 用于用户登录时的身份校验，查询用户完整信息（含加密后的密码、状态等）
     *
     * @param username 登录用户名（唯一索引字段，确保查询结果唯一）
     * @return 系统用户实体类，包含用户ID、用户名、加密密码、状态等所有字段；
     *         若未查询到对应用户名的用户，返回 null
     */
    SysUser selectByUsername(@Param("username") String username);
}