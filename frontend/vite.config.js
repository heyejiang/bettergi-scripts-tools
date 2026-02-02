// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
    plugins: [vue()],
    base: './',                    // 相对路径，兼容任意 context-path
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    server: {
        port: 5173,
        proxy: {
            '/bgi': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                secure: false
            }
        }
    }
})