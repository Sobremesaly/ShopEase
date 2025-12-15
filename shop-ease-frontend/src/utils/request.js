import { useUserStore } from '../stores/user';

// ===================== 全局配置 =====================
// 正在请求的接口缓存（防止重复提交）
const pendingRequests = new Set();
// 状态码提示映射（根据后端实际状态码调整）
const CODE_MESSAGE = {
    200: '操作成功',
    401: '登录已过期，请重新登录',
    403: '暂无权限访问',
    404: '接口不存在',
    500: '服务器内部错误',
    503: '服务暂时不可用，请稍后重试'
};

// ===================== 新增：Token刷新相关配置 =====================
// 标记是否正在刷新token（防止多次刷新）
let isRefreshing = false;
// 存储等待刷新token后重试的请求队列
let requestQueue = [];

// ===================== 核心请求函数 =====================
const request = (options) => {
    const userStore = useUserStore();
    // 解构配置（支持自定义loading、header、是否阻止重复请求）
    const {
        url,
        method = 'GET',
        data = {},
        header = {},
        showLoading = true, // 默认显示加载动画
        preventRepeat = true, // 默认阻止重复请求
        loadingText = '加载中...' // 加载提示文字
    } = options;

    // 1. 环境变量默认值（防止未配置报错）
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
    const fullUrl = baseUrl + url;

    // 2. 重复请求拦截（相同url+method视为重复）
    const requestKey = `${method}-${fullUrl}`;
    if (preventRepeat && pendingRequests.has(requestKey)) {
        console.warn(`已取消重复请求：${requestKey}`);
        return Promise.reject({ msg: '已取消重复请求' });
    }

    // 3. 显示加载动画
    if (showLoading) {
        uni.showLoading({ title: loadingText, mask: true }); // mask防止穿透点击
    }

    // 4. 加入请求缓存
    pendingRequests.add(requestKey);

    // 5. 创建请求控制器（支持取消请求）
    const controller = new AbortController();
    const signal = controller.signal;

    return new Promise((resolve, reject) => {
        // 合并默认header和自定义header（自定义header优先级更高）
        const defaultHeader = {
            'Content-Type': 'application/json',
            'Authorization': userStore.token ? `Bearer ${userStore.token}` : ''
        };
        const finalHeader = { ...defaultHeader, ...header };

        uni.request({
            url: fullUrl,
            method,
            data,
            header: finalHeader,
            signal, // 绑定取消控制器
            success: (res) => {
                // 区分：HTTP状态码（网络层面）和业务状态码（后端逻辑层面）
                const httpCode = res.statusCode;
                const businessData = res.data || {};
                const businessCode = businessData.code || httpCode;

                // 成功场景：HTTP200 + 业务200
                if (httpCode === 200 && businessCode === 200) {
                    resolve(businessData);
                    return;
                }

                // 错误场景：分类型提示
                const errorMsg = businessData.msg || CODE_MESSAGE[businessCode] || CODE_MESSAGE[httpCode] || '请求失败';
                uni.$u.toast(errorMsg);

                // ===================== 新增：处理Token过期（401） =====================
                if (businessCode === 401 || httpCode === 401) {
                    // 调用刷新Token的逻辑，刷新成功后重新发起请求
                    handleTokenExpired({
                        options,
                        resolve,
                        reject
                    });
                    return;
                }

                reject({ httpCode, businessCode, msg: errorMsg, data: businessData });
            },
            fail: (err) => {
                // 网络错误（无网络、接口不可达等）
                const errorMsg = err.message || '网络错误，请检查网络连接或后端服务';
                uni.$u.toast(errorMsg);
                console.error(`请求失败[${requestKey}]：`, err);
                reject({ msg: errorMsg, err });
            },
            complete: () => {
                // 无论成功/失败，都移除请求缓存+关闭加载动画
                pendingRequests.delete(requestKey);
                if (showLoading) {
                    uni.hideLoading();
                }
            }
        });

        // 暴露取消请求方法（调用方可手动取消）
        return {
            cancel: () => {
                controller.abort();
                pendingRequests.delete(requestKey);
                if (showLoading) uni.hideLoading();
                reject({ msg: '请求已取消' });
            }
        };
    });
};

// ===================== 新增：处理Token过期的核心函数 =====================
/**
 * 处理Token过期，刷新后重新发起请求
 * @param {Object} params - 包含请求配置、resolve、reject
 */
const handleTokenExpired = async ({ options, resolve, reject }) => {
    const userStore = useUserStore();
    const refreshToken = userStore.refreshToken; // 假设userStore中存储了refreshToken

    // 1. 无refreshToken，直接跳登录页
    if (!refreshToken) {
        userStore.logout(); // 清空token和用户信息
        uni.reLaunch({ url: '/pages/login/login' });
        reject({ msg: '登录状态失效，请重新登录' });
        return;
    }

    // 2. 如果正在刷新token，将请求加入队列等待
    if (isRefreshing) {
        requestQueue.push({ options, resolve, reject });
        return;
    }

    // 3. 开始刷新token
    isRefreshing = true;
    try {
        // 调用后端的/refreshToken接口（使用基础的uni.request，避免递归拦截）
        const refreshRes = await uni.request({
            url: (import.meta.env.VITE_API_BASE_URL || '/api') + '/sys/user/refreshToken',
            method: 'POST',
            header: {
                'Content-Type': 'application/json'
            },
            data: {
                refreshToken: refreshToken // 传递refreshTokenDTO的参数
            }
        });

        const refreshData = refreshRes.data || {};
        // 刷新成功：获取新的accessToken
        if (refreshRes.statusCode === 200 && refreshData.code === 200) {
            const newAccessToken = refreshData.data;
            // 更新userStore中的token
            userStore.token = newAccessToken; // 假设userStore中token是accessToken

            // 4. 重新发起队列中的所有请求
            requestQueue.forEach((item) => {
                // 重新调用request函数，传入更新后的配置
                request(item.options)
                    .then((res) => item.resolve(res))
                    .catch((err) => item.reject(err));
            });
            requestQueue = []; // 清空队列

            // 5. 重新发起当前失败的请求
            request(options).then((res) => resolve(res)).catch((err) => reject(err));
        } else {
            // 刷新失败（refreshToken过期）
            throw new Error(refreshData.msg || '刷新登录状态失败');
        }
    } catch (err) {
        // 刷新token失败，跳登录页
        uni.$u.toast(err.msg || '登录状态已过期，请重新登录');
        userStore.logout();
        uni.reLaunch({ url: '/pages/login/login' });
        reject(err);
    } finally {
        isRefreshing = false; // 结束刷新
    }
};

/**
 * POST请求
 * @param {string} url - 接口路径
 * @param {object} data - 请求参数
 * @param {object} options - 额外配置（showLoading/preventRepeat等）
 */
export const post = (url, data = {}, options = {}) => {
    return request({ url, method: 'POST', data, ...options });
};

/**
 * GET请求
 * @param {string} url - 接口路径
 * @param {object} params - 请求参数（会拼接到url）
 * @param {object} options - 额外配置（showLoading/preventRepeat等）
 */
export const get = (url, params = {}, options = {}) => {
    return request({ url, method: 'GET', data: params, ...options });
};

/**
 * PUT请求 - 完整更新资源
 */
export const put = (url, data = {}, options = {}) => {
    return request({
        url,
        method: 'PUT',
        data,
        ...options
    });
};

/**
 * PATCH请求 - 部分更新资源
 */
export const patch = (url, data = {}, options = {}) => {
    return request({
        url,
        method: 'PATCH',
        data,
        ...options
    });
};

/**
 * DELETE请求 - 删除资源
 */
export const del = (url, data = {}, options = {}) => {
    return request({
        url,
        method: 'DELETE',
        data,
        ...options
    });
};

/**
 * OPTIONS请求 - 预检请求
 */
export const options = (url, data = {}, options = {}) => {
    return request({
        url,
        method: 'OPTIONS',
        data,
        ...options
    });
};

/**
 * 上传文件（如头像、图片）
 * @param {string} url - 上传接口路径
 * @param {string} filePath - 本地文件路径（uni.chooseImage获取）
 * @param {object} options - 额外配置（name/fileType等）
 */
export const upload = (url, filePath, options = {}) => {
    const userStore = useUserStore();
    const {
        name = 'file', // 后端接收文件的字段名
        fileType = 'image', // 文件类型（image/video/audio）
        showLoading = true,
        loadingText = '上传中...',
        formData = {} // 额外的表单参数
    } = options;

    return new Promise((resolve, reject) => {
        if (showLoading) uni.showLoading({ title: loadingText, mask: true });

        uni.uploadFile({
            url: (import.meta.env.VITE_API_BASE_URL || '/api') + url,
            filePath,
            name,
            fileType,
            formData,
            header: {
                'Authorization': userStore.token ? `Bearer ${userStore.token}` : ''
            },
            success: (res) => {
                const data = JSON.parse(res.data || '{}');
                if (data.code === 200) {
                    resolve(data);
                } else {
                    // ===================== 新增：上传文件时处理401 =====================
                    if (data.code === 401) {
                        // 调用token过期处理逻辑，重新上传
                        handleTokenExpired({
                            options: {
                                url,
                                method: 'UPLOAD', // 自定义标识
                                data: { filePath, formData },
                                ...options
                            },
                            resolve,
                            reject
                        });
                        return;
                    }
                    const msg = data.msg || '上传失败';
                    uni.$u.toast(msg);
                    reject(data);
                }
            },
            fail: (err) => {
                uni.$u.toast('上传失败，请检查网络');
                reject(err);
            },
            complete: () => {
                if (showLoading) uni.hideLoading();
            }
        });
    });
};

export default request;