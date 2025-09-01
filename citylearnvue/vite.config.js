import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8088', //后端服务器的地址
        changeOrigin: true,
        pathRewrite: { '^/api': '' } // 重写路径，去掉 /api
      }
    }
  },
  plugins: [vue()],
})
