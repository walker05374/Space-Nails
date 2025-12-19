import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// --- VIEWS GERAIS ---
const LoginView = () => import('../views/autenticacao/LoginView.vue')
const RegistroView = () => import('../views/autenticacao/RegistroView.vue')
const RecuperarSenhaView = () => import('../views/autenticacao/RecuperarSenhaView.vue')
const ResetarSenhaView = () => import('../views/autenticacao/RedefinirSenhaView.vue') 
const HomeView = () => import('../views/inicio/HomeView.vue')
// Admin (Manter se for usar painel administrativo)
const AdminDashboard = () => import('../views/autenticacao/AdminDashboard.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    
    // Rotas Públicas
    { path: '/login', name: 'login', component: LoginView },
    { path: '/registro', name: 'registro', component: RegistroView },
    { path: '/recuperar-senha', name: 'recuperar-senha', component: RecuperarSenhaView },
    { path: '/resetar-senha', name: 'resetar-senha', component: ResetarSenhaView },

    // Rotas Privadas
    { 
      path: '/home', 
      name: 'home', 
      component: HomeView, 
      meta: { requiresAuth: true } 
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: AdminDashboard,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
  ]
})

// --- GUARDA DE ROTAS ---
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = !!authStore.token
  const publicPages = ['/login', '/registro', '/recuperar-senha', '/resetar-senha'];
  const authRequired = !publicPages.includes(to.path);

  // 1. Não logado -> Login
  if (authRequired && !isAuthenticated) {
    return next('/login');
  }

  // 2. Logado tentando ir para Login -> Home
  if (isAuthenticated && publicPages.includes(to.path)) {
    return next('/home');
  }

  next();
})

export default router;