// src/main.js
import './pages-json-js';
import { createSSRApp } from 'vue';
import { createPinia } from 'pinia';
import uView from 'uview-plus';
import App from './App.vue';
import 'uview-plus/index.scss';

export function createApp() {
	const app = createSSRApp(App);
	app.use(createPinia()); // 直接使用导入的 createPinia 函数
	app.use(uView);
	return { app };
}