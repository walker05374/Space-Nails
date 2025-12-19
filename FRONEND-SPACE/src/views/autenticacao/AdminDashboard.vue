<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const usuarios = ref([]);
const carregando = ref(true);

// Controle de Modais
const mostrarModalEdicao = ref(false);
const mostrarModalCadastro = ref(false);
const usuarioSelecionado = ref(null);

// Objeto para novo usuário
const novoUsuario = ref({
  nome: '',
  email: '',
  telefone: '',
  password: '',
  avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Space'
});

const stats = computed(() => {
  return {
    total: usuarios.value.length,
    ativos: usuarios.value.filter(u => u.ativo !== false).length,
    inativos: usuarios.value.filter(u => u.ativo === false).length
  };
});

onMounted(buscarUsuarios);

async function buscarUsuarios() {
  carregando.value = true;
  try {
    const response = await api.get('/api/admin/usuarios');
    usuarios.value = response.data;
  } catch (e) {
    console.error("Erro ao listar", e);
  } finally {
    carregando.value = false;
  }
}

async function cadastrarUsuario() {
  try {
    await api.post('/api/admin/profissionais', novoUsuario.value);
    alert("Tripulante cadastrado com sucesso!");
    mostrarModalCadastro.value = false;
    // Reseta o formulário
    novoUsuario.value = { nome: '', email: '', telefone: '', password: '', avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Space' };
    buscarUsuarios();
  } catch (e) {
    alert(e.response?.data?.message || "Erro ao cadastrar.");
  }
}

async function excluirUsuario(id) {
  if (!confirm("Remover este usuário permanentemente?")) return;
  try {
    await api.delete(`/api/admin/usuarios/${id}`);
    usuarios.value = usuarios.value.filter(u => u.id !== id);
  } catch (e) {
    alert(e.response?.data?.message || "Erro ao excluir.");
  }
}

async function alternarStatus(usuario) {
  if (usuario.role === 'ADMIN') return; // Bloqueio visual redundante
  try {
    await api.patch(`/api/admin/usuarios/${usuario.id}/status`);
    usuario.ativo = !usuario.ativo;
  } catch (e) {
    alert(e.response?.data?.message || "Erro ao mudar status.");
  }
}

function abrirEdicao(usuario) {
  usuarioSelecionado.value = { ...usuario };
  mostrarModalEdicao.value = true;
}

async function salvarEdicao() {
  try {
    await api.put(`/api/admin/usuarios/${usuarioSelecionado.value.id}`, usuarioSelecionado.value);
    buscarUsuarios();
    mostrarModalEdicao.value = false;
    alert("Dados atualizados!");
  } catch (e) {
    alert("Erro ao salvar.");
  }
}

function formatarData(data) {
  if (!data) return 'N/D';
  return new Date(data).toLocaleDateString('pt-BR');
}
</script>

<template>
  <div class="min-h-screen bg-[#0F071D] text-white font-sans pb-10">
    
    <header class="p-6 bg-[#1A1128] border-b border-purple-500/20 sticky top-0 z-30 flex justify-between items-center shadow-lg">
      <div class="flex flex-col">
        <h1 class="text-xl md:text-2xl font-black tracking-tighter">
          SPACE <span class="text-purple-500">ADMIN</span>
        </h1>
        <p class="text-[9px] text-purple-300/40 uppercase tracking-widest font-bold">Painel de Controle</p>
      </div>
      
      <button @click="authStore.logout" class="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 px-4 py-2 rounded-xl transition-all border border-red-500/20 group">
        <span class="text-xs font-black uppercase tracking-widest">Sair</span>
      </button>
    </header>

    <main class="p-4 md:p-8 max-w-7xl mx-auto space-y-8">
      
      <section class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="bg-[#1A1128] p-6 rounded-[30px] border border-purple-500/10 text-center">
          <span class="text-[10px] text-purple-300/40 uppercase font-black">Total Tripulantes</span>
          <p class="text-3xl font-black">{{ stats.total }}</p>
        </div>
        <div class="bg-[#1A1128] p-6 rounded-[30px] border border-green-500/10 text-center">
          <span class="text-[10px] text-green-300/40 uppercase font-black">Ativos</span>
          <p class="text-3xl font-black text-green-400">{{ stats.ativos }}</p>
        </div>
        <div class="bg-[#1A1128] p-6 rounded-[30px] border border-red-500/10 text-center">
          <span class="text-[10px] text-red-300/40 uppercase font-black">Em Repouso (Vencidos)</span>
          <p class="text-3xl font-black text-red-400">{{ stats.inativos }}</p>
        </div>
      </section>

      <section class="bg-[#1A1128] rounded-[40px] border border-purple-500/10 overflow-hidden shadow-2xl">
        <div class="p-6 border-b border-purple-500/10 flex justify-between items-center bg-purple-900/5">
          <h2 class="text-sm font-black uppercase tracking-widest text-purple-400">Gerenciar Tripulação</h2>
          <button @click="mostrarModalCadastro = true" class="bg-purple-600 text-[10px] font-black px-4 py-2 rounded-lg hover:bg-purple-500 transition-all shadow-lg shadow-purple-500/20">
            + NOVO PROFISSIONAL
          </button>
        </div>

        <div v-if="carregando" class="p-20 text-center animate-pulse text-purple-300 font-bold uppercase text-xs">Sincronizando órbita...</div>
        
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-purple-900/10">
              <tr>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase">Usuário</th>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase">Validade</th>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase text-center">Status</th>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-purple-500/5">
              <tr v-for="user in usuarios" :key="user.id" class="hover:bg-white/[0.02] transition-colors">
                <td class="p-4 flex items-center gap-3">
                  <img :src="user.avatarUrl" class="w-10 h-10 rounded-full border border-purple-500/30">
                  <div class="flex flex-col">
                    <span class="text-sm font-bold">{{ user.nome }}</span>
                    <span class="text-[10px] text-purple-400 font-bold uppercase tracking-tighter">{{ user.role }}</span>
                  </div>
                </td>
                
                <td class="p-4 text-xs font-bold text-purple-200/70">
                  {{ formatarData(user.dataValidade) }}
                </td>

                <td class="p-4 text-center">
                  <button 
                    @click="alternarStatus(user)"
                    :disabled="user.role === 'ADMIN'"
                    :class="['px-3 py-1 rounded-full text-[9px] font-black uppercase transition-all border', 
                      user.ativo ? 'bg-green-500/10 text-green-400 border-green-500/20' : 'bg-red-500/10 text-red-400 border-red-500/20',
                      user.role === 'ADMIN' ? 'opacity-50 cursor-not-allowed' : 'hover:scale-105']"
                  >
                    {{ user.ativo ? 'Ativo' : 'Suspenso' }}
                  </button>
                </td>

                <td class="p-4 text-right flex gap-2 justify-end">
                  <button @click="abrirEdicao(user)" class="p-2 bg-purple-500/10 text-purple-400 rounded-lg hover:bg-purple-500/20">📝</button>
                  <button 
                    @click="excluirUsuario(user.id)" 
                    v-if="user.role !== 'ADMIN'"
                    class="p-2 bg-red-500/10 text-red-400 rounded-lg hover:bg-red-500/20"
                  >🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <div v-if="mostrarModalCadastro" class="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div class="bg-[#1A1128] border border-purple-500/30 w-full max-w-md rounded-[30px] p-8 shadow-2xl">
        <h3 class="text-lg font-black text-purple-400 mb-6 uppercase tracking-widest">Novo Profissional</h3>
        <div class="space-y-4">
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">Nome Completo</label>
            <input v-model="novoUsuario.nome" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm focus:border-purple-500 outline-none">
          </div>
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">E-mail</label>
            <input v-model="novoUsuario.email" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm focus:border-purple-500 outline-none">
          </div>
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">Senha Inicial</label>
            <input type="password" v-model="novoUsuario.password" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm focus:border-purple-500 outline-none">
          </div>
          <div class="flex gap-3 pt-4">
            <button @click="mostrarModalCadastro = false" class="flex-1 py-3 bg-white/5 rounded-xl text-xs font-bold uppercase hover:bg-white/10 transition-all">Cancelar</button>
            <button @click="cadastrarUsuario" class="flex-1 py-3 bg-purple-600 rounded-xl text-xs font-bold uppercase hover:bg-purple-500 shadow-lg transition-all">Criar Acesso</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="mostrarModalEdicao" class="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div class="bg-[#1A1128] border border-purple-500/30 w-full max-w-md rounded-[30px] p-8 shadow-2xl">
        <h3 class="text-lg font-black text-purple-400 mb-6 uppercase tracking-widest">Editar / Renovar</h3>
        <div class="space-y-4">
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">Nome</label>
            <input v-model="usuarioSelecionado.nome" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm">
          </div>
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">Data de Validade (Assinatura)</label>
            <input type="date" v-model="usuarioSelecionado.dataValidade" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm focus:border-purple-500 outline-none">
          </div>
          <div class="flex gap-3 pt-4">
            <button @click="mostrarModalEdicao = false" class="flex-1 py-3 bg-white/5 rounded-xl text-xs font-bold uppercase">Cancelar</button>
            <button @click="salvarEdicao" class="flex-1 py-3 bg-purple-600 rounded-xl text-xs font-bold uppercase">Atualizar</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>