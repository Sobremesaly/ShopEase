import { createSSRApp } from 'vue';
import { createPinia } from 'pinia';
import { useUserStore } from './stores/user';
import 'uview-plus/index.scss';
import './pages-json-js';
import uView from 'uview-plus';
import 'uview-plus/index.scss';
import App from './App.vue';
// 不需要登录的页面白名单（必须配置完整）
const WHITE_LIST = [
	'/pages/login/login',
	'/pages/register/register',
	'/pages/forgot-pwd/forgot-pwd',
	// 后续新增的无需登录页面，都要加这里
];

export function createApp() {
	const app = createSSRApp(App);
	const pinia = createPinia();
	app.use(pinia);
	app.use(uView);
	// 1. App启动时校验登录态（关键！处理直接打开App/刷新页面）
	const userStore = useUserStore();
	const checkLoginOnLaunch = () => {
		// 获取当前页面路径（兼容H5/小程序）
		const pages = getCurrentPages();
		const currentPage = pages[pages.length - 1] || {};
		const currentPath = currentPage.route ? `/${currentPage.route}` : '';

		// 不在白名单 + 未登录 → 强制跳登录页
		if (!WHITE_LIST.includes(currentPath) && !userStore.isLogin()) {
			uni.reLaunch({ url: '/pages/login/login' });
		}
	};

	// 执行启动校验
	checkLoginOnLaunch();

	// 2. 统一拦截所有跳转方式（核心防御）
	const interceptAllNavigations = (options) => {
		const userStore = useUserStore();
		// 获取目标页面路径（兼容带参数的URL，如 /pages/login/login?phone=123）
		const targetPath = options.url.split('?')[0];

		// 不在白名单 + 未登录 → 阻止跳转+跳登录页
		if (!WHITE_LIST.includes(targetPath) && !userStore.isLogin()) {
			uni.$u.toast('请先登录');
			// 强制跳登录页（关闭所有页面，避免回退）
			uni.reLaunch({ url: '/pages/login/login' });
			return false; // 阻止原跳转
		}
		return true; // 允许跳转
	};

	// 给所有跳转API加拦截
	['navigateTo', 'redirectTo', 'reLaunch', 'switchTab', 'navigateBack'].forEach((method) => {
		uni.addInterceptor(method, {
			invoke(options) {
				return interceptAllNavigations(options);
			},
			fail(err) {
				console.error(`跳转拦截失败：${method}`, err);
			}
		});
	});

	return { app, pinia };
}
