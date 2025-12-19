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
    authStore.setLoginData({ id: userData.userId, nome: userData.nome, email: userData.email, role: userData.role, avatar: userData.avatar }, userData.token);
    
    if (userData.role === 'ADMIN') {
        router.push('/admin');
    } else {
        router.push('/home');
    }

  } catch (e) {
    const msg = e.response?.data?.message;
    
    if (msg === "ASSINATURA_EXPIRADA" || msg === "CONTA_SUSPENSA") {
      const whatsMsg = encodeURIComponent(`Olá! Meu acesso ao Space Nails foi bloqueado (E-mail: ${email.value}). Gostaria de renovar minha assinatura.`);
      const linkWhatsapp = `https://wa.me/5599999999999?text=${whatsMsg}`; // COLOQUE SEU NUMERO AQUI
      
      if (confirm("Sua assinatura expirou ou está suspensa. Deseja falar com o suporte no WhatsApp para renovar?")) {
        window.open(linkWhatsapp, '_blank');
      }
      erro.value = "Acesso suspenso ou expirado.";
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
  <div class="min-h-screen bg-[#0F071D] flex items-center justify-center px-4 font-sans relative overflow-hidden">
    <div class="w-full max-w-md bg-[#1A1128] p-10 md:p-14 rounded-[40px] shadow-[0_20px_60px_rgba(139,92,246,0.4)] border border-purple-500/20 relative z-10">
      <div class="text-center mb-10">
        <h1 class="text-4xl font-black text-white tracking-tighter">SPACE<span class="text-purple-500">NAILS</span></h1>
        <p class="text-purple-300/60 text-xs mt-2 font-bold uppercase tracking-widest">Management System</p>
      </div>

      <form @submit.prevent="fazerLogin" class="space-y-6">
        <div>
          <label class="label-space">E-mail Profissional</label>
          <input type="email" v-model="email" required :disabled="!bancoOnline" class="input-space" placeholder="seu@email.com">
        </div>
        <div>
          <label class="label-space">Senha de Acesso</label>
          <input type="password" v-model="password" required :disabled="!bancoOnline" class="input-space" placeholder="••••••••">
        </div>
        <div v-if="erro" class="bg-red-900/20 border border-red-500/50 rounded-2xl p-4 flex items-center gap-3 animate-shake">
          <p class="text-red-400 text-xs font-bold leading-tight">{{ erro }}</p>
        </div>
        <button type="submit" :disabled="isSubmitting || !bancoOnline || verificandoConexao" class="btn-space">
          <span v-if="isSubmitting" class="animate-spin">⏳</span>
          <span v-else>INICIAR MISSÃO</span>
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.label-space { @apply block text-purple-300/50 font-black mb-2 ml-1 text-[10px] uppercase tracking-widest; }
.input-space { @apply w-full px-6 py-4 rounded-2xl bg-white/5 border border-purple-500/20 focus:border-purple-500 focus:bg-white/10 outline-none transition-all text-white font-bold placeholder-purple-900/50; }
.btn-space { @apply w-full py-5 rounded-2xl font-black text-sm tracking-[0.2em] shadow-lg flex justify-center items-center gap-2 bg-gradient-to-r from-purple-600 to-indigo-600 text-white; }
</style>