
import { useUserStore } from '@/stores/user';
// 创建请求实例（直接用全局 uni.request，无需导入）
const request = (options) => {
    const userStore = useUserStore();
    return new Promise((resolve, reject) => {
        // 直接使用全局 uni.request（无需加前缀）
        uni.request({
            // 环境变量 + 接口路径（.env.development 已配置为 /api）
            url: import.meta.env.VITE_API_BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json', // JSON 传参（后端要求 form-data 可修改）
                'Authorization': userStore.token ? `Bearer ${userStore.token}` : ''
            },
            success: (res) => {
                // 注意：UniApp 的 res.data 是后端返回的完整数据，需根据后端格式判断
                if (res.data.code !== 200) {
                    uni.$u.toast(res.data.msg || '请求失败');
                    reject(res.data);
                    return;
                }
                resolve(res.data);
            },
            fail: (err) => {
                uni.$u.toast('网络错误，请检查后端服务是否正常');
                console.error('请求失败：', err);
                reject(err);
            }
        });
    });
};

// 封装 POST/GET 请求（不变）
export const post = (url, data) => {
    return request({ url, method: 'POST', data });
};

export const get = (url, data) => {
    return request({ url, method: 'GET', data });
};

export default request;