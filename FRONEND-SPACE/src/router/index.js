import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Importação das Views
import LoginView from '../views/autenticacao/LoginView.vue'
import HomeView from '../views/inicio/HomeView.vue'
import AdminDashboard from '../views/autenticacao/AdminDashboard.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { publico: true } // Marca como pública
    },
    // --- ROTAS DE SENHA (PÚBLICAS) ---
    {
      path: '/recuperar-senha',
      name: 'recuperar-senha',
      component: () => import('../views/autenticacao/RecuperarSenhaView.vue'),
      meta: { publico: true } // Importante: Permite acesso sem login
    },
    {
      path: '/redefinir-senha', // O link do e-mail apontará para cá
      name: 'redefinir-senha',
      component: () => import('../views/autenticacao/RedefinirSenhaView.vue'),
      meta: { publico: true } // Importante: Permite acesso sem login
    },
    {
      path: '/agendar/:slug',
      name: 'booking',
      component: () => import('../views/publico/PublicBookingView.vue'),
      meta: { publico: true }
    },
    {
      path: '/portfolio/public/:inviteCode',
      name: 'public-portfolio',
      component: () => import('../views/publico/PublicPortfolioView.vue'),
      meta: { publico: true }
    },
    // --- ROTAS PROTEGIDAS ---
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminDashboard,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    // Rota para qualquer URL não encontrada volta pro login
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

// --- GUARDA DE NAVEGAÇÃO (Segurança) ---
router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  const estaLogado = !!auth.token; // Verifica se tem token
  const ehRotaPublica = to.meta.publico;

  // 1. Se a rota NÃO é pública e o usuário NÃO está logado -> Manda pro Login
  if (!ehRotaPublica && !estaLogado) {
    return next('/login');
  }

  // 2. Se o usuário JÁ está logado e tenta ir pro Login -> Manda pra Home (ou Admin)
  if (ehRotaPublica && estaLogado && to.path === '/login') {
    return next(auth.user?.role === 'ADMIN' ? '/admin' : '/home');
  }

  // 3. Verificação de Permissão de Admin
  if (to.meta.role === 'ADMIN' && auth.user?.role !== 'ADMIN') {
    return next('/home'); // Se tentar acessar admin sem ser admin, vai pra home
  }

  next(); // Segue o fluxo normal
})

export default router