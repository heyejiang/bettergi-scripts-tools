// src/router.js
import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'home',
        component:()=> import('@main/views/HomeView.vue')
    },
    // 其他路由...
]

const router = createRouter({
    history: createWebHistory(),
    routes: routes,
})
router.beforeEach((to, from, next) => {
    next()
})

export default router