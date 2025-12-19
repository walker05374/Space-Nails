<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api'; 

const route = useRoute();
const router = useRouter();
const token = ref(null);
const novaSenha = ref('');
const erro = ref(null);
const sucesso = ref(null);
const loading = ref(false);

onMounted(() => {
  token.value = route.query.token;
  if (!token.value) erro.value = 'Link inválido ou expirado.';
});

async function redefinir() {
  loading.value = true;
  erro.value = null;
  
  try {
    await api.post('/api/auth/reset-password', {
      token: token.value,
      newPassword: novaSenha.value
    });
    sucesso.value = 'Senha alterada com sucesso! Redirecionando...';
    setTimeout(() => router.push('/login'), 3000);
  } catch (e) {
    erro.value = 'Erro ao redefinir senha. O link pode ter expirado.';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] flex items-center justify-center px-4 font-sans">
    
    <div class="bg-white p-10 rounded-3xl shadow-lg shadow-gray-200/50 border border-gray-100 w-full max-w-md text-center">
      
      <div class="w-16 h-16 bg-pink-50 rounded-full flex items-center justify-center mx-auto mb-6 text-2xl shadow-sm">
        ✨
      </div>

      <h1 class="text-2xl font-bold text-[#0F172A] mb-6">Nova Senha</h1>
      
      <form v-if="token && !sucesso" @submit.prevent="redefinir" class="space-y-6">
        <input 
          type="password" 
          v-model="novaSenha" 
          required 
          placeholder="Digite sua nova senha"
          class="w-full px-5 py-4 rounded-2xl bg-gray-50 border-none outline-none focus:ring-2 focus:ring-[#DB2777] text-[#0F172A] font-medium transition-all placeholder-gray-400"
        >
        
        <button 
          type="submit" 
          :disabled="loading"
          class="w-full bg-[#DB2777] text-white font-bold py-4 rounded-2xl shadow-lg shadow-pink-500/30 hover:brightness-105 transition-all uppercase tracking-wider text-sm disabled:opacity-50"
        >
          {{ loading ? 'SALVANDO...' : 'SALVAR NOVA SENHA' }}
        </button>
      </form>

      <div v-if="sucesso" class="p-4 bg-green-50 text-green-600 rounded-2xl font-bold text-sm border border-green-100 animate-pulse">
        {{ sucesso }}
      </div>
      
      <div v-if="erro" class="p-4 bg-red-50 text-red-500 rounded-2xl font-bold text-sm border border-red-100">
        {{ erro }}
      </div>
    </div>
  </div>
</template>