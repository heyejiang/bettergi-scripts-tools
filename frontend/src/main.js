// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router.js'  // 如果有 router.js

const app = createApp(App)

if (router) {
    app.use(router)
}

app.mount('#app')