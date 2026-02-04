// src/router.js
import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'home',
        isRoot: true,
        meta: {
            title: '首页',
            desc: '首页',
            asSubParentTitle: '主页功能',
            icon: 'icon-home'
        },
        component: () => import('@main/views/HomeView'),
        children: [
            {
                path: '/bgi/ui', name: 'bgi', meta: {
                    title: '测试',
                    desc: '测试',
                    asSubParentTitle: '测试功能',
                    icon: 'icon-home'
                },
            }
        ]
    },
    // 其他路由...
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: routes,
})
router.beforeEach((to, from, next) => {
    console.log('Navigating to:', to.path);
    next()
})

export default router