// src/stores/user.js（Pinia 用户状态库）
import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    // 状态：存储用户账号、密码（记住密码时）、token
    state: () => ({
        account: '',
        password: '',
        rememberPwd: false,
        token: ''
    }),
    // 持久化：刷新页面后仍保留状态（关键！）
    persist: {
        enabled: true, // 开启持久化
        strategies: [
            {
                storage: uni.getStorageSync ? uni.storage : window.localStorage, // 适配 H5/小程序
                paths: ['account', 'password', 'rememberPwd', 'token'] // 需要持久化的字段
            }
        ]
    },
    // 方法：修改用户信息
    actions: {
        setUserInfo(info) {
            this.account = info.account;
            this.password = info.password;
            this.rememberPwd = info.rememberPwd;
            this.token = info.token;
        },
        // 退出登录：清空状态
        logout() {
            this.$reset(); // 重置所有状态
        }
    }
});