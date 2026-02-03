import { createApp } from 'vue'
import App from './App.vue'
import router from './router/router.js'

const app = createApp(App)

// 等 Vue 挂载完再隐藏欢迎屏
app.use(router).mount('#app')

// 延迟触发事件，通知欢迎页隐藏
window.dispatchEvent(new Event('vue:mounted'))
