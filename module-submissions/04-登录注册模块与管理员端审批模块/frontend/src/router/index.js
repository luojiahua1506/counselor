import { createRouter, createWebHistory } from 'vue-router'
const routes = [
  { path: '/login', component: () => import('../views/counselor/Login.vue'), meta: { guest: true } },
  { path: '/register', component: () => import('../views/counselor/Register.vue'), meta: { guest: true } },
  { path: '/', component: () => import('../views/counselor/CounselorLayout.vue'), meta: { requiresAuth: true, role: 'COUNSELOR' }, children: [
    { path: '', component: () => import('../views/counselor/Dashboard.vue') },
    { path: 'batch/:id/fill', component: () => import('../views/counselor/BatchFill.vue') },
    { path: 'contacts', component: () => import('../views/counselor/Contacts.vue') },
    { path: 'profile', component: () => import('../views/counselor/Profile.vue') },
    { path: 'profile/edit', component: () => import('../views/counselor/ProfileEdit.vue') }
    ,{ path: 'psych', component: () => import('../views/counselor/PsychBatches.vue') }
    ,{ path: 'psych/batches/:id', component: () => import('../views/counselor/PsychAssessment.vue') }
    ,{ path: 'psych/records/:id/report', component: () => import('../views/counselor/PsychReport.vue') }
    ,{ path: 'notifications', component: () => import('../views/counselor/Notifications.vue') }
  ]},
  { path: '/admin/login', component: () => import('../views/admin/Login.vue'), meta: { guest: true } },
  { path: '/admin', component: () => import('../views/admin/AdminLayout.vue'), meta: { requiresAuth: true, role: 'ADMIN' }, children: [
    { path: '', component: () => import('../views/admin/Dashboard.vue') },
    { path: 'batches', component: () => import('../views/admin/Batches.vue') },
    { path: 'batches/:id', component: () => import('../views/admin/BatchDetail.vue') },
    { path: 'counselors', component: () => import('../views/admin/Counselors.vue') },
    { path: 'counselors/:id', component: () => import('../views/admin/CounselorDetail.vue') },
    { path: 'registrations', component: () => import('../views/admin/Registrations.vue') },
    { path: 'profile-edits', component: () => import('../views/admin/ProfileEdits.vue') },
    { path: 'colleges', component: () => import('../views/admin/Colleges.vue') },
    { path: 'logs', component: () => import('../views/admin/Logs.vue') }
    ,{ path: 'psych/dashboard', component: () => import('../views/admin/PsychDashboard.vue') }
    ,{ path: 'psych/batches', component: () => import('../views/admin/PsychBatches.vue') }
    ,{ path: 'psych/alerts', component: () => import('../views/admin/PsychAlerts.vue') }
  ]},
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach(function(to, from, next) {
  var isAdmin = to.path.indexOf('/admin') === 0
  var prefix = isAdmin ? 'admin_' : 'counselor_'
  var token = localStorage.getItem(prefix + 'accessToken')
  var role = localStorage.getItem(prefix + 'role')
  if (to.meta.requiresAuth && !token) return next(isAdmin ? '/admin/login' : '/login')
  if (to.meta.requiresAuth && to.meta.role && to.meta.role !== role) return next(isAdmin ? '/admin/login' : '/login')
  next()
})
export default router
