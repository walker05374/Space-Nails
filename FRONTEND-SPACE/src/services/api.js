import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

// Cria a instância do Axios
const api = axios.create({
  // Se estiver no Docker (VITE_API_URL=/), usa rota relativa.
  // Se rodar local sem docker (npm run dev), tenta localhost:8080.
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true // Importante para sessões/cookies se houver
});

// --- INTERCEPTOR DE REQUISIÇÃO (Envia o Token) ---
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- INTERCEPTOR DE RESPOSTA (Tratamento de Erros) ---
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // 1. Erro de login incorreto não deve deslogar, apenas retornar erro
    if (error.config && error.config.url.includes('/auth/login')) {
      return Promise.reject(error);
    }

    // 2. Se der 401 (Não autorizado) ou 403 (Proibido)
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.warn('Sessão expirada ou inválida. Redirecionando para login...');
      
      const authStore = useAuthStore();
      if (authStore.clearLoginData) {
        authStore.clearLoginData();
      }
      
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default api;