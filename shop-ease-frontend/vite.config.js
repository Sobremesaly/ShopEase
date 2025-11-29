// 根目录 vite.config.js（uView 配套配置）
import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

export default defineConfig({
  plugins: [uni()],
  resolve: {
  },
  // 支持 SCSS（uView 样式依赖）
  css: {
    preprocessorOptions: {
      scss: {
        // 全局导入 uView 的 SCSS 变量（可选，方便自定义主题）
        additionalData: '@import "uview-plus/theme.scss";'
      }
    }
  },
  // 排除 u-xxx 组件的原生元素判断（避免警告）
  compilerOptions: {
    isCustomElement: (tag) => tag.startsWith('u-') || tag.startsWith('uni-')
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});