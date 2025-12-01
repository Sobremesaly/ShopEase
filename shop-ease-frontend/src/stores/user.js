import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    // 状态：存储用户信息、token、记住密码状态（同步本地缓存）
    state: () => ({
        account: uni.getStorageSync('account') || '', // 账号（本地持久化）
        password: uni.getStorageSync('password') || '', // 密码（记住密码时存储）
        rememberPwd: uni.getStorageSync('rememberPwd') || false, // 记住密码开关
        token: uni.getStorageSync('token') || '', // 登录态token（核心！）
        userInfo: uni.getStorageSync('userInfo') || {} // 用户信息（头像、昵称等）
    }),
    // 方法：修改状态的逻辑（集中管理，避免散写）
    actions: {
        // 1. 登录成功：设置用户信息（你代码里调用的 setUserInfo 方法）
        setUserInfo(data) {
            this.account = data.account;
            this.password = data.rememberPwd ? data.password : ''; // 不记住密码则清空
            this.rememberPwd = data.rememberPwd;
            this.token = data.token;
            this.userInfo = data.userInfo || {};
            this.avatar = data.avatar || '/static/icons10.png';
            // 同步到本地缓存（持久化，刷新/重启APP不丢失）
            uni.setStorageSync('account', this.account);
            uni.setStorageSync('password', this.password);
            uni.setStorageSync('rememberPwd', this.rememberPwd);
            uni.setStorageSync('token', this.token);
            uni.setStorageSync('userInfo', this.userInfo);
        },
        // 2. 退出登录：清空所有状态（后续会用到）
        logout() {
            this.account = '';
            this.password = '';
            this.rememberPwd = false;
            this.token = '';
            this.userInfo = {};

            // 清空本地缓存
            uni.removeStorageSync('account');
            uni.removeStorageSync('password');
            uni.removeStorageSync('rememberPwd');
            uni.removeStorageSync('token');
            uni.removeStorageSync('userInfo');
        },
        // 3. 校验登录态：判断token是否存在（后续权限拦截用）
        isLogin() {
            return !!this.token;
        }
    }
});
