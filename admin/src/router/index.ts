import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { title: '仪表盘' },
    },
    {
      path: '/accounts',
      name: 'accounts',
      component: () => import('@/views/Accounts.vue'),
      meta: { title: '账号管理' },
    },
    {
      path: '/courses',
      name: 'courses',
      component: () => import('@/views/Courses.vue'),
      meta: { title: '课程管理' },
    },
    {
      path: '/activities',
      name: 'activities',
      component: () => import('@/views/Activities.vue'),
      meta: { title: '签到活动' },
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/Settings.vue'),
      meta: { title: '设置' },
    },
  ],
})

router.afterEach((to) => {
  document.title = `${to.meta.title} · 签到管理`
})

export default router
