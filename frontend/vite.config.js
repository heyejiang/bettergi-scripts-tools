// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
    plugins: [vue()],
    base: '/bgi/ui/',                    // 相对路径，兼容任意 context-path
    resolve: {
        alias: {
            '@views': path.resolve(__dirname, 'src/views'), // 添加 @views 别名指向 views 目录
            '@style': path.resolve(__dirname, 'src/assets/style'), // 添加 @views 别名指向 views 目录
            '@css': path.resolve(__dirname, 'src/assets/style/css'), // 添加 @css 别名指向 css 目录
            '@scss': path.resolve(__dirname, 'src/assets/style/scss'), // 添加 @scss 别名指向 css/scss 目录
            '@assets': path.resolve(__dirname, 'src/assets'), // 添加 @assets 别名指向 assets 目录
            '@api': path.resolve(__dirname, 'src/api'), // 添加 @api 别名指向 api 目录
            '@utils': path.resolve(__dirname, 'src/utils'), // 添加 @util 别名指向 util 目录
            '@router': path.resolve(__dirname, 'src/router'), // 添加 @router 别名指向 router 目录
            '@store': path.resolve(__dirname, 'src/store'), // 添加 @store 别名指向 store 目录
            '@layout': path.resolve(__dirname, 'src/layout'), // 添加 @layout 别名指向 layout 目录
            '@components': path.resolve(__dirname, 'src/components'), // 添加 @components 别名指向 components 目录
            '@plugins': path.resolve(__dirname, 'src/plugins'), // 添加 @plugins 别名指向 plugins 目录
            '@main': path.resolve(__dirname, 'src'), // 添加 @main 别名指向 src 目录
            '@': path.resolve(__dirname, 'src/'), // 假设 src/ 目录位于项目根目录下
        }
    },
    // server: {
    //     port: 5173,
    //     proxy: {
    //         '/bgi': {
    //             target: 'http://localhost:8081',
    //             changeOrigin: true,
    //             secure: false
    //         }
    //     }
    // }
})