import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
  },
  {
    path: '/student',
    component: () => import('@/views/student/Layout.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT'] },
    children: [
      { path: '', redirect: '/student/internships' },
      {
        path: 'internships',
        name: 'StudentInternships',
        component: () => import('@/views/student/Internships.vue'),
      },
      {
        path: 'internships/create',
        name: 'CreateInternship',
        component: () => import('@/views/student/InternshipForm.vue'),
      },
      {
        path: 'internships/:id/edit',
        name: 'EditInternship',
        component: () => import('@/views/student/InternshipForm.vue'),
      },
      {
        path: 'reports',
        name: 'StudentReports',
        component: () => import('@/views/student/Reports.vue'),
      },
      {
        path: 'reports/create',
        name: 'CreateReport',
        component: () => import('@/views/student/ReportForm.vue'),
      },
      {
        path: 'reports/:id/edit',
        name: 'EditReport',
        component: () => import('@/views/student/ReportForm.vue'),
      },
      {
        path: 'locations',
        name: 'StudentLocations',
        component: () => import('@/views/student/Locations.vue'),
      },
      {
        path: 'evaluations',
        name: 'StudentEvaluations',
        component: () => import('@/views/student/Evaluations.vue'),
      },
    ],
  },
  {
    path: '/teacher',
    component: () => import('@/views/teacher/Layout.vue'),
    meta: { requiresAuth: true, roles: ['TEACHER'] },
    children: [
      { path: '', redirect: '/teacher/students' },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: () => import('@/views/teacher/Students.vue'),
      },
      {
        path: 'internships',
        name: 'TeacherInternships',
        component: () => import('@/views/teacher/Internships.vue'),
      },
      {
        path: 'reports',
        name: 'TeacherReports',
        component: () => import('@/views/teacher/Reports.vue'),
      },
      {
        path: 'locations',
        name: 'TeacherLocations',
        component: () => import('@/views/teacher/Locations.vue'),
      },
      {
        path: 'map',
        name: 'TeacherMap',
        component: () => import('@/views/teacher/MapView.vue'),
      },
      {
        path: 'evaluation',
        name: 'TeacherEvaluation',
        component: () => import('@/views/teacher/EvaluationForm.vue'),
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
      },
      {
        path: 'companies',
        name: 'AdminCompanies',
        component: () => import('@/views/admin/Companies.vue'),
      },
      {
        path: 'internships',
        name: 'AdminInternships',
        component: () => import('@/views/admin/Internships.vue'),
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
      },
    ],
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const hasToken = !!userStore.token && userStore.token.length > 0
  const userRole = userStore.user?.role

  // 1. 未登录访问需要权限的页面
  if (to.meta.requiresAuth && !hasToken) {
    return next('/login')
  }

  // 2. 角色权限校验
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    if (!userRole || !requiredRoles.includes(userRole)) {
      return next('/login')
    }
  }

  // 3. 已登录不能访问登录/注册页
  if ((to.path === '/login' || to.path === '/register') && hasToken) {
    if (!userRole) {
      userStore.logout()
      return next('/login')
    }
    if (userRole === 'STUDENT') return next('/student/internships')
    if (userRole === 'TEACHER') return next('/teacher/students')
    return next('/admin/dashboard')
  }

  next()
})

export default router
