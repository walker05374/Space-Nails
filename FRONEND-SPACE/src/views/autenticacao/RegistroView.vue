<script setup>
import { ref } from 'vue';
import api from '@/services/api'; 
import { useRouter } from 'vue-router';
import AvatarSelectorModal from '@/components/AvatarSelectorModal.vue'; 

const router = useRouter();

// Estado do Modal de Avatar
const mostrarSeletorAvatar = ref(false);
const quemEstaSelecionando = ref(null);

// Estado dos Termos de Uso
const aceitouTermos = ref(false);
const mostrarModalTermos = ref(false);

// Dados do Profissional (Usuario)
const nome = ref('');
const email = ref('');
const telefone = ref('');
const password = ref('');
const confirmarSenha = ref('');
const avatarUrl = ref('https://api.dicebear.com/7.x/micah/svg?seed=Profissional&backgroundColor=f3e8ff'); 

// Controle de Senha
const mostrarSenha = ref(false);
const mostrarConfirmar = ref(false);

// Dados dos Clientes Iniciais
const clientes = ref([
  { nome: '', telefone: '', observacoes: '' }
]);

const erro = ref(null);
const isSubmitting = ref(false);

// --- Lógica de Avatar ---
function abrirSeletor(tipo, index = null) {
  quemEstaSelecionando.value = { tipo, index };
  mostrarSeletorAvatar.value = true;
}

function salvarAvatarSelecionado(url) {
  const { tipo, index } = quemEstaSelecionando.value;
  if (tipo === 'PROFISSIONAL') {
    avatarUrl.value = url;
  }
  mostrarSeletorAvatar.value = false;
}

function adicionarCliente() {
  clientes.value.push({ nome: '', telefone: '', observacoes: '' });
}

function removerCliente(index) {
  if (clientes.value.length > 1) {
    clientes.value.splice(index, 1);
  }
}

async function registrar() {
  erro.value = null;

  if (!aceitouTermos.value) {
    erro.value = "Você precisa aceitar os Termos de Uso.";
    return;
  }

  // Validação de Senha Forte
  const temTamanho = password.value.length >= 8;
  const temMaiuscula = /[A-Z]/.test(password.value);
  const temNumero = /[0-9]/.test(password.value);
  if (!temTamanho || !temMaiuscula || !temNumero) {
    erro.value = 'A senha deve conter no mínimo 8 caracteres, uma letra maiúscula e um número.';
    return;
  }
  
  if (password.value !== confirmarSenha.value) { 
    erro.value = "As senhas não coincidem!"; 
    return; 
  }

  isSubmitting.value = true;
  
  try {
    // 1. Criar Profissional (Baseado no RegisterRequestDTO)
    await api.post('/auth/register', {
      nome: nome.value,
      email: email.value,
      password: password.value,
      telefone: telefone.value,
      avatarUrl: avatarUrl.value
    });
    
    // 2. Login automático para obter o token e criar os clientes
    const loginResponse = await api.post('/api/auth/login', {
      email: email.value,
      password: password.value
    });
    
    const token = loginResponse.data.token;
    // Salva o token temporariamente para as próximas requisições
    const config = { headers: { Authorization: `Bearer ${token}` } };

    // 3. Criar Clientes (Baseado no ClienteController)
    for (const cliente of clientes.value) {
      if (cliente.nome) {
        await api.post('/api/clientes', {
          nome: cliente.nome,
          telefone: cliente.telefone,
          observacoes: cliente.observacoes
        }, config);
      }
    }
    
    alert('Conta criada com sucesso! ✨ Bem-vinda ao Space Nails.');
    router.push('/login');

  } catch (e) {
    erro.value = e.response?.data?.message || 'Erro ao criar conta. Verifique os dados.';
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0F071D] py-10 px-4 font-sans flex items-center justify-center relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl animate-pulse opacity-40">⭐</div>
    <div class="absolute bottom-20 right-20 text-4xl animate-bounce opacity-30">🚀</div>

    <div class="bg-[#1A1128] p-8 rounded-[40px] shadow-[0_20px_50px_rgba(139,92,246,0.3)] w-full max-w-lg border border-purple-500/30 relative z-10">
      
      <div class="text-center mb-8">
        <h1 class="text-3xl font-extrabold text-white mb-1">
          <span class="text-purple-400">Space</span> Nails
        </h1>
        <p class="text-purple-300/60 text-sm font-medium">Cadastre seu perfil profissional</p>
      </div>

      <form @submit.prevent="registrar" class="space-y-6">
        
        <div class="bg-purple-900/10 p-6 rounded-[30px] border border-purple-500/20 shadow-inner">
          <h3 class="text-purple-400 font-bold text-center mb-6 border-b border-purple-500/20 pb-2 uppercase text-xs tracking-widest">
            ✨ Seus Dados
          </h3>
          
          <div class="flex flex-col items-center mb-6">
            <div class="cursor-pointer group relative" @click="abrirSeletor('PROFISSIONAL')">
              <div class="w-24 h-24 rounded-full border-2 border-purple-500 overflow-hidden bg-purple-900/30">
                <img :src="avatarUrl" class="w-full h-full object-cover">
              </div>
              <div class="absolute -bottom-2 left-1/2 transform -translate-x-1/2 bg-purple-600 text-white text-[10px] font-bold px-3 py-1 rounded-full">
                Trocar
              </div>
            </div>
          </div>

          <div class="space-y-4">
            <input type="text" v-model="nome" required class="input-space" placeholder="Seu Nome Completo">
            <input type="email" v-model="email" required class="input-space" placeholder="Seu E-mail">
            <input type="text" v-model="telefone" required class="input-space" placeholder="Telefone (ex: 11999999999)">

            <div class="grid grid-cols-2 gap-4">
              <div class="relative">
                <input :type="mostrarSenha ? 'text' : 'password'" v-model="password" required class="input-space pr-10" placeholder="Senha">
                <button type="button" @click="mostrarSenha = !mostrarSenha" class="absolute right-3 top-3 text-purple-300">
                  {{ mostrarSenha ? '🙈' : '👁️' }}
                </button>
              </div>
              <div class="relative">
                <input :type="mostrarConfirmar ? 'text' : 'password'" v-model="confirmarSenha" required class="input-space pr-10" placeholder="Confirmar">
                <button type="button" @click="mostrarConfirmar = !mostrarConfirmar" class="absolute right-3 top-3 text-purple-300">
                  {{ mostrarConfirmar ? '🙈' : '👁️' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white/5 p-6 rounded-[30px] border border-white/10 relative">
          <h3 class="text-white font-bold text-center mb-6 text-xs uppercase tracking-widest">
            💅 Meus Primeiros Clientes
          </h3>

          <div v-for="(cliente, index) in clientes" :key="index" class="mb-4 space-y-3 relative pb-4 border-b border-white/5 last:border-0">
            <button v-if="clientes.length > 1" type="button" @click="removerCliente(index)" class="absolute right-0 -top-2 text-red-400 text-[10px] font-bold">REMOVER</button>
            
            <input type="text" v-model="cliente.nome" class="input-space text-sm" placeholder="Nome do Cliente">
            <div class="grid grid-cols-2 gap-2">
              <input type="text" v-model="cliente.telefone" class="input-space text-xs" placeholder="WhatsApp">
              <input type="text" v-model="cliente.observacoes" class="input-space text-xs" placeholder="Obs (opcional)">
            </div>
          </div>

          <button type="button" @click="adicionarCliente" class="w-full mt-2 py-2 border border-dashed border-purple-500/40 text-purple-400 text-xs font-bold rounded-xl hover:bg-purple-500/10 transition-all">
            + Adicionar outro cliente
          </button>
        </div>

        <div class="flex items-center gap-3 px-2">
          <input type="checkbox" id="termos" v-model="aceitouTermos" class="rounded border-purple-500 bg-transparent text-purple-600 focus:ring-purple-500">
          <label for="termos" class="text-xs text-purple-200/70">
            Aceito os <span @click.prevent="mostrarModalTermos = true" class="text-purple-400 underline cursor-pointer">Termos de Uso</span>.
          </label>
        </div>

        <div v-if="erro" class="text-red-400 text-xs text-center font-bold bg-red-900/20 p-3 rounded-2xl border border-red-500/20">{{ erro }}</div>

        <button type="submit" :disabled="isSubmitting" class="w-full bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-bold py-4 rounded-2xl shadow-lg hover:brightness-110 active:scale-95 transition-all disabled:opacity-50">
          {{ isSubmitting ? 'ESTRELA EM ÓRBITA...' : 'FINALIZAR CADASTRO ✨' }}
        </button>
      </form>
      
      <div class="mt-8 text-center">
        <router-link to="/login" class="text-purple-300/50 hover:text-purple-300 text-xs font-bold transition-colors">
          JÁ TENHO UMA NAVE? <span class="underline">FAZER LOGIN</span>
        </router-link>
      </div>
    </div>

    <AvatarSelectorModal v-if="mostrarSeletorAvatar" @close="mostrarSeletorAvatar = false" @select="salvarAvatarSelecionado" />

    <div v-if="mostrarModalTermos" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-[#1A1128] rounded-[30px] border border-purple-500/30 w-full max-w-2xl max-h-[80vh] flex flex-col overflow-hidden">
        <div class="p-6 border-b border-purple-500/20 flex justify-between items-center bg-purple-900/10">
          <h3 class="text-xl font-bold text-purple-400">📜 Regras da Galáxia</h3>
          <button @click="mostrarModalTermos = false" class="text-purple-300 hover:text-white font-bold text-xl">✕</button>
        </div>
        <div class="p-8 overflow-y-auto text-purple-200/80 text-sm space-y-4">
          <p>1. Seus dados serão usados para gestão de agendamentos.</p>
          <p>2. Os dados dos clientes são de sua responsabilidade exclusiva.</p>
          <p class="text-xs italic text-purple-500 mt-4">Space Nails - 2025.</p>
        </div>
        <div class="p-6 border-t border-purple-500/20 bg-purple-900/10 text-right">
          <button @click="aceitouTermos = true; mostrarModalTermos = false" class="bg-purple-600 text-white font-bold px-8 py-3 rounded-xl hover:bg-purple-700 transition-all">
            CONCORDO
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.input-space {
  @apply w-full px-4 py-3 rounded-2xl bg-white/5 border border-purple-500/20 focus:border-purple-500 focus:bg-white/10 outline-none transition-all text-white placeholder-purple-300/30 font-medium;
}
</style>