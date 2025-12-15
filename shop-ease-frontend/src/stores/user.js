import { defineStore } from 'pinia';
// 引入请求工具（假设你有封装的axios/uni.request请求模块，替换为你的实际路径）
import request from '../utils/request';

// 统一管理本地存储的Key（避免硬编码，便于维护）
const STORAGE_KEYS = {
    ACCOUNT: 'user_account',
    PASSWORD: 'user_password',
    REMEMBER_PWD: 'user_remember_pwd',
    TOKEN: 'user_access_token',
    REFRESH_TOKEN: 'user_refresh_token',
    USER_INFO: 'user_info',
    AVATAR: 'user_avatar'
};

// 定义用户信息的类型（可选，提升代码可读性，Vue3+TS可使用interface）
/**
 * 后端返回的LoginVO结构
 * @typedef {Object} LoginVO
 * @property {number} userId - 用户ID
 * @property {string} username - 用户名
 * @property {string} nickname - 昵称
 * @property {string} token - Access Token
 * @property {string} refreshToken - Refresh Token
 */

/**
 * 用户状态管理Store
 */
export const useUserStore = defineStore('user', {
    // 状态：存储用户信息、双Token、记住密码状态（同步本地缓存）
    state: () => ({
        // 账号相关（记住密码用）
        account: uni.getStorageSync(STORAGE_KEYS.ACCOUNT) || '', // 用户名/账号
        password: uni.getStorageSync(STORAGE_KEYS.PASSWORD) || '', // 密码（可选加密存储）
        rememberPwd: uni.getStorageSync(STORAGE_KEYS.REMEMBER_PWD) || false, // 记住密码开关
        // 认证相关（双Token）
        token: uni.getStorageSync(STORAGE_KEYS.TOKEN) || '', // Access Token（核心鉴权）
        refreshToken: uni.getStorageSync(STORAGE_KEYS.REFRESH_TOKEN) || '', // Refresh Token（刷新用）
        // 用户信息
        userInfo: uni.getStorageSync(STORAGE_KEYS.USER_INFO) || {}, // 用户基本信息（userId、nickname等）
        avatar: uni.getStorageSync(STORAGE_KEYS.AVATAR) || '/static/icons10.png' // 头像（修复原代码未定义的问题）
    }),

    // 计算属性：简化状态判断（可选，提升代码简洁性）
    getters: {
        // 校验是否已登录（判断Access Token是否存在）
        isLogin: (state) => !!state.token,
        // 获取用户ID（从userInfo中提取）
        userId: (state) => state.userInfo.userId || ''
    },

    // 方法：修改状态的逻辑（集中管理）
    actions: {
        /**
         * 1. 登录成功：存储用户信息和双Token（适配后端LoginVO结构）
         * @param {Object} params - 登录参数
         * @param {LoginVO} params.loginVO - 后端返回的登录结果
         * @param {string} params.account - 输入的账号（用户名）
         * @param {string} params.password - 输入的密码
         * @param {boolean} params.rememberPwd - 是否记住密码
         */
        setUserInfo({ loginVO, account, password, rememberPwd }) {
            // 1. 存储账号和密码（记住密码则保留，否则清空）
            this.account = account;
            this.password = rememberPwd ? this.encryptPwd(password) : ''; // 可选：密码加密存储
            this.rememberPwd = rememberPwd;

            // 2. 存储双Token（核心）
            this.token = loginVO.token; // Access Token
            this.refreshToken = loginVO.refreshToken; // Refresh Token

            // 3. 存储用户信息（过滤敏感数据，只保留必要字段）
            this.userInfo = {
                userId: loginVO.userId,
                username: loginVO.username,
                nickname: loginVO.nickname
                // 可补充其他字段：phone、avatar等
            };

            // 4. 存储头像（优先使用后端返回的，无则用默认）
            this.avatar = loginVO.avatar || '/static/icons10.png';

            // 5. 同步到本地缓存（持久化）
            uni.setStorageSync(STORAGE_KEYS.ACCOUNT, this.account);
            uni.setStorageSync(STORAGE_KEYS.PASSWORD, this.password);
            uni.setStorageSync(STORAGE_KEYS.REMEMBER_PWD, this.rememberPwd);
            uni.setStorageSync(STORAGE_KEYS.TOKEN, this.token);
            uni.setStorageSync(STORAGE_KEYS.REFRESH_TOKEN, this.refreshToken);
            uni.setStorageSync(STORAGE_KEYS.USER_INFO, this.userInfo);
            uni.setStorageSync(STORAGE_KEYS.AVATAR, this.avatar);
        },

        /**
         * 2. 刷新Access Token（调用后端接口）
         * @returns {Promise<boolean>} - 刷新是否成功
         */
        async refreshAccessToken() {
            try {
                // 校验Refresh Token是否存在
                if (!this.refreshToken) {
                    uni.showToast({ title: '刷新令牌不存在，请重新登录', icon: 'none' });
                    this.logout(); // 清空状态
                    return false;
                }

                // 调用后端刷新Token接口（替换为你的实际接口路径）
                const res = await request({
                    url: '/sys/user/refreshToken',
                    method: 'POST',
                    data: { refreshToken: this.refreshToken }
                });

                // 刷新成功：更新Access Token
                if (res.code === 200) {
                    this.updateAccessToken(res.data); // res.data是新的Access Token
                    return true;
                } else {
                    uni.showToast({ title: res.msg || '刷新令牌失败', icon: 'none' });
                    this.logout(); // 刷新失败，清空状态
                    return false;
                }
            } catch (error) {
                console.error('刷新Token失败：', error);
                uni.showToast({ title: '网络异常，刷新失败', icon: 'none' });
                this.logout(); // 异常情况，清空状态
                return false;
            }
        },

        /**
         * 3. 更新Access Token（单独更新，不修改其他状态）
         * @param {string} newToken - 新的Access Token
         */
        updateAccessToken(newToken) {
            this.token = newToken;
            uni.setStorageSync(STORAGE_KEYS.TOKEN, newToken);
        },

        /**
         * 4. 退出登录：清空所有状态 + 调用后端接口清理Refresh Token
         */
        async logout() {
            try {
                // 可选：调用后端退出登录接口（清理Redis中的Refresh Token）
                if (this.refreshToken) {
                    await request({
                        url: '/sys/user/logout',
                        method: 'POST',
                        data: { refreshToken: this.refreshToken }
                    });
                }
            } catch (error) {
                console.error('退出登录接口调用失败：', error);
                // 接口调用失败不影响本地状态清空
            } finally {
                // 清空本地状态
                this.account = '';
                this.password = '';
                this.rememberPwd = false;
                this.token = '';
                this.refreshToken = '';
                this.userInfo = {};
                this.avatar = '/static/icons10.png';

                // 清空本地缓存
                uni.removeStorageSync(STORAGE_KEYS.ACCOUNT);
                uni.removeStorageSync(STORAGE_KEYS.PASSWORD);
                uni.removeStorageSync(STORAGE_KEYS.REMEMBER_PWD);
                uni.removeStorageSync(STORAGE_KEYS.TOKEN);
                uni.removeStorageSync(STORAGE_KEYS.REFRESH_TOKEN);
                uni.removeStorageSync(STORAGE_KEYS.USER_INFO);
                uni.removeStorageSync(STORAGE_KEYS.AVATAR);

                // 可选：跳转到登录页
                uni.redirectTo({ url: '/pages/login/login' });
            }
        },

        /**
         * 辅助方法：密码简单加密（可选，提升安全性，避免明文存储）
         * @param {string} pwd - 原始密码
         * @returns {string} - 加密后的密码
         */
        encryptPwd(pwd) {
            // 示例：base64加密（可替换为更安全的加密方式，如md5+盐）
            return btoa(unescape(encodeURIComponent(pwd)));
        },

        /**
         * 辅助方法：密码解密（对应encryptPwd）
         * @param {string} encryptedPwd - 加密后的密码
         * @returns {string} - 原始密码
         */
        decryptPwd(encryptedPwd) {
            try {
                return decodeURIComponent(escape(atob(encryptedPwd)));
            } catch (error) {
                return '';
            }
        }
    }
});