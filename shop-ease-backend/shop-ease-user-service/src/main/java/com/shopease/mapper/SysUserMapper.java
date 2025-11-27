package com.shopease.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shopease.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // 根据用户名查询用户（登录时用）
    SysUser selectByUsername(@Param("username") String username);
}
