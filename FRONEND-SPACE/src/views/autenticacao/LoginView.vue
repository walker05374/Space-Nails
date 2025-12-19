<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api'; 
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const password = ref(''); 
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
    await api.get('/api/health');
    bancoOnline.value = true;
  } catch (e) {
    bancoOnline.value = false;
    erro.value = "⚠️ Sem conexão com o servidor.";
  } finally {
    verificandoConexao.value = false;
  }
}

async function fazerLogin() {
  if (!bancoOnline.value) return; 
  erro.value = null;
  isSubmitting.value = true;
  
  try {
    const response = await api.post('/api/auth/login', {
      email: email.value,
      password: password.value
    });
    
    const userData = response.data;
    
    // CORREÇÃO AQUI: Adicionado 'dataValidade' ao objeto salvo
    authStore.setLoginData({ 
        id: userData.userId, 
        nome: userData.nome, 
        email: userData.email, 
        role: userData.role, 
        avatar: userData.avatar,
        dataValidade: userData.dataValidade // <--- IMPORTANTE
    }, userData.token);
    
    if (userData.role === 'ADMIN') {
        router.push('/admin');
    } else {
        router.push('/home');
    }

  } catch (e) {
    const msg = e.response?.data?.message;
    if (msg === "ASSINATURA_EXPIRADA" || msg === "CONTA_SUSPENSA") {
      erro.value = "Acesso suspenso. Entre em contato com o suporte.";
    } else if (e.response?.status === 401) {
      erro.value = 'E-mail ou senha incorretos.';
    } else {
      erro.value = 'Erro ao processar login.';
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] flex items-center justify-center px-4 font-sans relative">
    
    <div class="absolute top-0 left-0 w-full h-64 bg-gradient-to-b from-pink-100/50 to-transparent -z-10"></div>

    <div class="w-full max-w-md bg-white p-10 rounded-3xl shadow-lg shadow-gray-200/50 border border-gray-100 relative z-10">
      
      <div class="text-center mb-10">
        <h1 class="text-3xl font-bold text-[#0F172A] tracking-tight">Studio<span class="text-[#DB2777]">.Nails</span></h1>
        <p class="text-gray-400 text-sm mt-2 font-medium">Bem-vinda de volta</p>
      </div>

      <form @submit.prevent="fazerLogin" class="space-y-6">
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase mb-2 ml-1">E-mail Profissional</label>
          <input type="email" v-model="email" required :disabled="!bancoOnline" class="w-full p-4 bg-gray-50 border-none rounded-2xl text-sm font-medium focus:ring-2 focus:ring-[#DB2777] outline-none text-[#0F172A] placeholder-gray-400 transition-all" placeholder="seu@email.com">
        </div>
        
        <div>
          <label class="block text-xs font-bold text-gray-400 uppercase mb-2 ml-1">Senha</label>
          <input type="password" v-model="password" required :disabled="!bancoOnline" class="w-full p-4 bg-gray-50 border-none rounded-2xl text-sm font-medium focus:ring-2 focus:ring-[#DB2777] outline-none text-[#0F172A] placeholder-gray-400 transition-all" placeholder="••••••••">
          
          <div class="text-right mt-2">
            <router-link to="/recuperar-senha" class="text-xs font-bold text-gray-400 hover:text-[#DB2777] transition-colors">
              Esqueci minha senha
            </router-link>
          </div>
        </div>

        <div v-if="erro" class="bg-red-50 text-red-500 p-4 rounded-2xl text-xs font-bold text-center border border-red-100">
          {{ erro }}
        </div>

        <button type="submit" :disabled="isSubmitting || !bancoOnline || verificandoConexao" class="w-full py-4 rounded-2xl font-bold text-sm shadow-lg shadow-pink-500/30 flex justify-center items-center gap-2 bg-[#DB2777] text-white hover:brightness-110 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed uppercase tracking-wider">
          <span v-if="isSubmitting">Acessando...</span>
          <span v-else>ENTRAR</span>
        </button>
      </form>
    </div>
  </div>
</template>