import { createApp } from 'vue'
import App from './App.vue'
import router from './router/router'
import { initApiPrefix } from './utils/http'


// 初始化前端应用
async function bootstrap() {
    await initApiPrefix() // 获取动态 API 前缀
    const app = createApp(App)

    // 等 Vue 挂载完再隐藏欢迎屏
    app.use(router).mount('#app')
}

bootstrap()
