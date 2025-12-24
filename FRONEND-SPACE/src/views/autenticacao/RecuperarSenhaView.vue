<script setup>
import { ref } from 'vue';
import api from '@/services/api'; 

const email = ref('');
const mensagem = ref(null);
const erro = ref(null);
const isSubmitting = ref(false);

async function solicitar() {
  mensagem.value = null; 
  erro.value = null; 
  isSubmitting.value = true;

  try {
    await api.post('/api/auth/forgot-password', { email: email.value });
    mensagem.value = `Link enviado! Verifique seu e-mail.`;
  } catch (e) {
    erro.value = "Não foi possível enviar o link. Verifique o e-mail.";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] flex items-center justify-center px-4 font-sans relative">
    
    <div class="absolute top-0 right-0 w-full h-64 bg-gradient-to-b from-pink-50/50 to-transparent -z-10"></div>

    <div class="w-full max-w-md bg-white p-8 rounded-3xl shadow-lg shadow-gray-200/50 border border-gray-100 text-center relative z-10">
      
      <div class="mb-2">
        <img src="/icon.png" alt="Icon" class="w-36 h-36 mx-auto animate-bounce-slow" />
      </div>

      <h1 class="text-2xl font-bold text-[#0F172A] mb-2">Recuperar Senha</h1>
      <p class="text-gray-400 text-sm mb-8 font-medium">Digite seu e-mail para receber o link de redefinição.</p>
      
      <form @submit.prevent="solicitar" class="space-y-6">
        <input 
          type="email" 
          v-model="email" 
          required 
          placeholder="Seu e-mail cadastrado" 
          class="w-full px-5 py-4 rounded-2xl bg-gray-50 border-none outline-none focus:ring-2 focus:ring-[#DB2777] text-[#0F172A] font-medium transition-all placeholder-gray-400"
        >

        <button 
          type="submit" 
          :disabled="isSubmitting" 
          class="w-full bg-[#DB2777] text-white font-bold py-4 rounded-2xl shadow-lg shadow-pink-500/30 hover:brightness-105 transition-all disabled:opacity-50 uppercase tracking-wider text-sm"
        >
          {{ isSubmitting ? 'ENVIANDO...' : 'ENVIAR LINK' }}
        </button>
      </form>
      
      <div v-if="mensagem" class="mt-6 p-4 bg-green-50 text-green-600 rounded-2xl font-bold text-sm border border-green-100">
        {{ mensagem }}
      </div>
      
      <div v-if="erro" class="mt-6 p-4 bg-red-50 text-red-500 rounded-2xl font-bold text-sm border border-red-100">
        {{ erro }}
      </div>
      
      <div class="mt-8 pt-6 border-t border-gray-50">
        <router-link to="/login" class="text-gray-400 hover:text-[#DB2777] text-xs font-bold transition-colors uppercase tracking-wide">
          ← Voltar para o Login
        </router-link>
      </div>
    </div>
  </div>
</template>