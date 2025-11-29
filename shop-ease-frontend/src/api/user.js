import { request } from '@/utils/request';

// 登录接口
export const login = (data) => request({ url: '/sys/user/login', method: 'POST', data });
// 注册接口
export const register = (data) => request({ url: '/sys/user/register', method: 'POST', data });
// 获取用户信息接口
export const getUserInfo = () => request({ url: '/sys/user/current', method: 'GET' });