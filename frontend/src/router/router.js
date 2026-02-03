// src/router.js
import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'home',
        component: () => import('@main/views/HomeView'),
        // children: [
        //     {path: '/bgi/ui', name: 'bgi', component: () => import('@main/views/BgiView')}
        // ]
    },
    // 其他路由...
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: routes,
})
router.beforeEach((to, from, next) => {
    next()
})

export default router