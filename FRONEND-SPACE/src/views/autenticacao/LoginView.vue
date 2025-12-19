<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api'; 
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const password = ref(''); // Ajustado para bater com LoginRequestDTO do Java
const erro = ref(null);
const isSubmitting = ref(false);
const verificandoConexao = ref(true);
const bancoOnline = ref(false);

onMounted(async () => {
  await checarStatusSistema();
});

async function checarStatusSistema() {
  verificandoConexao.value = true;
  erro.value = null;
  try {
    // Health check do backend
    await api.get('/api/health');
    bancoOnline.value = true;
  } catch (e) {
    if (e.code === 'ERR_NETWORK') {
        bancoOnline.value = false;
        erro.value = "⚠️ Sem conexão com o servidor da Space Nails.";
    } else {
        bancoOnline.value = true;
    }
  } finally {
    verificandoConexao.value = false;
  }
}

async function fazerLogin() {
  if (!bancoOnline.value) return; 

  erro.value = null;
  isSubmitting.value = true;
  
  try {
    // Rota definida no AuthController.java
    const response = await api.post('/api/auth/login', {
      email: email.value,
      password: password.value
    });
    
    const userData = response.data; // LoginResponseDTO

    // Salva no Pinia Store
    authStore.setLoginData(
      { 
        id: userData.userId,
        nome: userData.nome, 
        email: userData.email, 
        role: userData.role, 
        avatar: userData.avatar 
      },
      userData.token
    );
    
    // Redirecionamento baseado no Role do Usuario.java
    if (userData.role === 'ADMIN') {
        router.push('/admin');
    } else {
        router.push('/home'); // Onde ele gerencia seus clientes
    }

  } catch (e) {
    console.error(e);
    if (e.response && (e.response.status === 401 || e.response.status === 403)) {
      erro.value = 'E-mail ou senha incorretos.';
    } else {
      erro.value = 'Erro ao processar login no sistema.';
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0F071D] flex items-center justify-center px-4 font-sans relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl animate-pulse opacity-40">⭐</div>
    <div class="absolute top-24 right-20 text-2xl animate-bounce opacity-20">💅</div>
    <div class="absolute bottom-20 left-1/4 text-4xl animate-pulse opacity-30">🚀</div>
    <div class="absolute bottom-10 right-10 text-5xl opacity-20 rotate-12">✨</div>

    <div class="w-full max-w-md bg-[#1A1128] p-10 md:p-14 rounded-[40px] shadow-[0_20px_60px_rgba(139,92,246,0.4)] border border-purple-500/20 relative z-10">
      
      <div class="text-center mb-10">
        <h1 class="text-4xl font-black text-white tracking-tighter">
          SPACE<span class="text-purple-500">NAILS</span>
        </h1>
        <p class="text-purple-300/60 text-xs mt-2 font-bold uppercase tracking-widest">Management System</p>

        <div class="mt-6 flex justify-center">
           <div v-if="verificandoConexao" class="text-[10px] text-purple-300 font-bold bg-purple-900/30 px-4 py-1 rounded-full animate-pulse border border-purple-500/20">
             📡 SINCRONIZANDO ÓRBITA...
           </div>
           <div v-else-if="bancoOnline" class="text-[10px] text-green-400 font-bold bg-green-900/20 border border-green-500/20 px-4 py-1 rounded-full flex items-center gap-2">
             <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span> SISTEMA ONLINE
           </div>
           <div v-else class="text-[10px] text-red-400 font-bold bg-red-900/20 border border-red-500/20 px-4 py-1 rounded-full cursor-pointer" @click="checarStatusSistema">
             🔴 SEM SINAL (RECONECTAR)
           </div>
        </div>
      </div>

      <form @submit.prevent="fazerLogin" class="space-y-6">
        
        <div>
          <label class="label-space">E-mail Profissional</label>
          <input 
            type="email" 
            v-model="email" 
            required
            :disabled="!bancoOnline" 
            class="input-space"
            placeholder="seu@email.com"
          >
        </div>

        <div>
          <label class="label-space">Senha de Acesso</label>
          <input 
            type="password" 
            v-model="password" 
            required
            :disabled="!bancoOnline"
            class="input-space"
            placeholder="••••••••"
          >
        </div>

        <div v-if="erro" class="bg-red-900/20 border border-red-500/50 rounded-2xl p-4 flex items-center gap-3 animate-shake">
          <p class="text-red-400 text-xs font-bold leading-tight">{{ erro }}</p>
        </div>

        <button 
          type="submit" 
          :disabled="isSubmitting || !bancoOnline || verificandoConexao"
          class="btn-space"
        >
          <span v-if="isSubmitting" class="animate-spin">⏳</span>
          <span v-else>INICIAR MISSÃO</span>
        </button>
      </form>

      <div class="mt-10 text-center">
        <router-link to="/recuperar-senha" class="text-purple-300/40 text-xs font-bold hover:text-purple-400 transition-colors uppercase tracking-widest">
          Esqueci minha senha
        </router-link>
      </div>

    </div>
  </div>
</template>

<style scoped>
.label-space { @apply block text-purple-300/50 font-black mb-2 ml-1 text-[10px] uppercase tracking-widest; }
.input-space { @apply w-full px-6 py-4 rounded-2xl bg-white/5 border border-purple-500/20 focus:border-purple-500 focus:bg-white/10 outline-none transition-all text-white font-bold placeholder-purple-900/50 shadow-inner; }
.btn-space { @apply w-full py-5 rounded-2xl font-black text-sm tracking-[0.2em] shadow-lg transform transition-all active:scale-95 flex justify-center items-center gap-2 bg-gradient-to-r from-purple-600 to-indigo-600 text-white hover:brightness-110 disabled:opacity-30 disabled:cursor-not-allowed; }

@keyframes shake { 0%, 100% { transform: translateX(0); } 25% { transform: translateX(-5px); } 75% { transform: translateX(5px); } }
.animate-shake { animation: shake 0.3s ease-in-out; }
</style>