<script setup>
import { ref } from 'vue';
import api from '@/services/api'; 
import { useRouter } from 'vue-router';
import AvatarSelectorModal from '@/components/AvatarSelectorModal.vue'; 

const router = useRouter();

// Estados
const mostrarSeletorAvatar = ref(false);
const quemEstaSelecionando = ref(null);
const aceitouTermos = ref(false);
const mostrarModalTermos = ref(false);
const erro = ref(null);
const isSubmitting = ref(false);

// Dados
const nome = ref('');
const email = ref('');
const telefone = ref('');
const password = ref('');
const confirmarSenha = ref('');
const avatarUrl = ref('https://api.dicebear.com/7.x/micah/svg?seed=Nails&backgroundColor=fce7f3'); 
const mostrarSenha = ref(false);

const clientes = ref([{ nome: '', telefone: '', observacoes: '' }]);

function abrirSeletor(tipo, index = null) {
  quemEstaSelecionando.value = { tipo, index };
  mostrarSeletorAvatar.value = true;
}

function salvarAvatarSelecionado(url) {
  if (quemEstaSelecionando.value.tipo === 'PROFISSIONAL') {
    avatarUrl.value = url;
  }
  mostrarSeletorAvatar.value = false;
}

function adicionarCliente() { clientes.value.push({ nome: '', telefone: '', observacoes: '' }); }
function removerCliente(index) { if (clientes.value.length > 1) clientes.value.splice(index, 1); }

async function registrar() {
  erro.value = null;
  if (!aceitouTermos.value) return erro.value = "Necessário aceitar os Termos.";
  
  // Validação Simplificada para UX (pode manter a regex forte se desejar)
  if (password.value.length < 6) return erro.value = 'A senha deve ter no mínimo 6 caracteres.';
  if (password.value !== confirmarSenha.value) return erro.value = "As senhas não coincidem."; 

  isSubmitting.value = true;
  
  try {
    await api.post('/auth/register', {
      nome: nome.value,
      email: email.value,
      password: password.value,
      telefone: telefone.value,
      avatarUrl: avatarUrl.value
    });
    
    // Login automático e criação de clientes mantidos
    const loginResponse = await api.post('/api/auth/login', { email: email.value, password: password.value });
    const config = { headers: { Authorization: `Bearer ${loginResponse.data.token}` } };

    for (const cliente of clientes.value) {
      if (cliente.nome) {
        await api.post('/api/clientes', cliente, config);
      }
    }
    
    alert('Cadastro realizado com sucesso!');
    router.push('/login');
  } catch (e) {
    erro.value = e.response?.data?.message || 'Erro ao criar conta.';
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-brand-bg py-10 px-4 font-sans flex items-center justify-center">
    
    <div class="w-full max-w-2xl bg-white rounded-3xl shadow-float border border-gray-100 overflow-hidden">
      <div class="p-8 md:p-12">
        
        <div class="text-center mb-10">
          <h1 class="text-2xl font-bold text-brand-dark">Crie sua conta</h1>
          <p class="text-brand-gray text-sm mt-2">Comece a organizar seu negócio hoje.</p>
        </div>

        <form @submit.prevent="registrar" class="space-y-8">
          
          <div class="space-y-6">
            <div class="flex flex-col items-center">
              <div class="relative group cursor-pointer" @click="abrirSeletor('PROFISSIONAL')">
                <img :src="avatarUrl" class="w-24 h-24 rounded-full border-4 border-gray-50 shadow-sm object-cover bg-gray-50">
                <div class="absolute bottom-0 right-0 bg-brand-dark text-white p-2 rounded-full shadow-md hover:bg-brand-primary transition-colors">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
                </div>
              </div>
              <span class="text-xs font-bold text-gray-400 mt-2 uppercase">Foto de Perfil</span>
            </div>

            <div class="grid md:grid-cols-2 gap-4">
              <input type="text" v-model="nome" required class="input-modern" placeholder="Nome Completo">
              <input type="text" v-model="telefone" required class="input-modern" placeholder="WhatsApp">
            </div>
            <input type="email" v-model="email" required class="input-modern w-full" placeholder="Seu melhor e-mail">
            
            <div class="grid md:grid-cols-2 gap-4">
              <input :type="mostrarSenha ? 'text' : 'password'" v-model="password" required class="input-modern" placeholder="Senha">
              <input :type="mostrarSenha ? 'text' : 'password'" v-model="confirmarSenha" required class="input-modern" placeholder="Confirmar Senha">
            </div>
          </div>

          <hr class="border-gray-100">

          <div class="bg-gray-50 p-6 rounded-2xl border border-gray-100">
            <h3 class="text-sm font-bold text-brand-dark uppercase tracking-wide mb-4">Adicionar Primeiros Clientes (Opcional)</h3>
            
            <div v-for="(cliente, index) in clientes" :key="index" class="mb-4 pb-4 border-b border-gray-200 last:border-0 relative">
              <button v-if="clientes.length > 1" type="button" @click="removerCliente(index)" class="absolute right-0 top-0 text-red-400 text-xs font-bold hover:text-red-600">Excluir</button>
              
              <div class="grid gap-3">
                <input type="text" v-model="cliente.nome" class="input-modern bg-white" placeholder="Nome da Cliente">
                <div class="grid grid-cols-2 gap-3">
                  <input type="text" v-model="cliente.telefone" class="input-modern bg-white text-xs" placeholder="WhatsApp">
                  <input type="text" v-model="cliente.observacoes" class="input-modern bg-white text-xs" placeholder="Obs">
                </div>
              </div>
            </div>

            <button type="button" @click="adicionarCliente" class="w-full py-3 border-2 border-dashed border-gray-300 text-gray-500 rounded-xl text-xs font-bold hover:border-brand-primary hover:text-brand-primary transition-all">
              + Adicionar Outra Cliente
            </button>
          </div>

          <div class="flex items-center gap-3">
            <input type="checkbox" id="termos" v-model="aceitouTermos" class="w-5 h-5 rounded border-gray-300 text-brand-primary focus:ring-brand-primary">
            <label for="termos" class="text-xs text-gray-500 font-medium">
              Li e aceito os <span @click.prevent="mostrarModalTermos = true" class="text-brand-dark underline cursor-pointer font-bold">Termos de Uso</span>.
            </label>
          </div>

          <div v-if="erro" class="text-red-500 text-xs font-bold text-center bg-red-50 p-3 rounded-xl border border-red-100">{{ erro }}</div>

          <button type="submit" :disabled="isSubmitting" class="w-full bg-brand-primary text-white font-bold py-4 rounded-2xl shadow-lg shadow-brand-primary/20 hover:brightness-105 transition-all disabled:opacity-50">
            {{ isSubmitting ? 'Cadastrando...' : 'Finalizar Cadastro' }}
          </button>

          <p class="text-center text-xs text-gray-400 font-bold">
            Já tem conta? <router-link to="/login" class="text-brand-primary hover:underline">Fazer Login</router-link>
          </p>
        </form>
      </div>
    </div>
    
    <AvatarSelectorModal v-if="mostrarSeletorAvatar" @close="mostrarSeletorAvatar = false" @select="salvarAvatarSelecionado" />

    <div v-if="mostrarModalTermos" class="fixed inset-0 bg-brand-dark/20 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="font-bold text-brand-dark">Termos de Uso</h3>
          <button @click="mostrarModalTermos = false" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>
        <div class="p-6 text-sm text-gray-600 space-y-4">
          <p>1. O uso da plataforma é exclusivo para gestão de serviços de beleza.</p>
          <p>2. Seus dados e os de seus clientes são confidenciais.</p>
        </div>
        <div class="p-6 bg-gray-50 text-right">
          <button @click="aceitouTermos = true; mostrarModalTermos = false" class="bg-brand-dark text-white font-bold px-6 py-2 rounded-xl text-sm">Concordo</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.input-modern {
  @apply w-full px-4 py-3 rounded-xl bg-gray-50 border border-transparent focus:bg-white focus:border-brand-primary/50 focus:ring-2 focus:ring-brand-primary/10 outline-none transition-all text-brand-dark placeholder-gray-400 font-medium text-sm;
}
</style>