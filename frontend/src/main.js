import { createApp } from 'vue'
import App from '@main/App'
import router from '@router/router'
import { initApiPrefix } from '@utils/http'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 初始化前端应用
async function bootstrap() {
    await initApiPrefix() // 获取动态 API 前缀
    const app = createApp(App)
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
        app.component(key, component)
    }
    // 等 Vue 挂载完再隐藏欢迎屏
    app.use(router).mount('#app')
}

bootstrap()
