import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/login/Login.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/dashboard/Index.vue'), meta: { title: '工作台' } },
        { path: 'member', component: () => import('@/views/member/Index.vue'), meta: { title: '会员管理' } },
        { path: 'card-type', component: () => import('@/views/card/CardType.vue'), meta: { title: '会员卡类型' } },
        { path: 'card-instance', component: () => import('@/views/card/CardInstance.vue'), meta: { title: '会员卡实例' } },
        { path: 'course-type', component: () => import('@/views/course/CourseType.vue'), meta: { title: '课程类型' } },
        { path: 'room', component: () => import('@/views/room/Index.vue'), meta: { title: '教室管理' } },
        { path: 'coach', component: () => import('@/views/coach/Index.vue'), meta: { title: '教练管理' } },
        { path: 'schedule', component: () => import('@/views/course/Schedule.vue'), meta: { title: '排课管理' } },
        { path: 'booking', component: () => import('@/views/booking/Index.vue'), meta: { title: '预约管理' } },
        { path: 'order', component: () => import('@/views/order/Index.vue'), meta: { title: '订单管理' } },
        { path: 'statistics', component: () => import('@/views/statistics/Index.vue'), meta: { title: '数据统计' } },
        { path: 'system', component: () => import('@/views/system/Index.vue'), meta: { title: '系统设置' } }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const store = useUserStore()
  if (to.meta.public) return next()
  if (!store.token) return next('/login')
  next()
})

export default router
