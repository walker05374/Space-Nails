<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const usuarios = ref([]);
const carregando = ref(true);
const feedback = ref(null);

// Estados de Modais e Edição
const mostrarModalEdicao = ref(false);
const usuarioSelecionado = ref(null);

// Estatísticas Computadas
const stats = computed(() => {
  return {
    total: usuarios.value.length,
    ativos: usuarios.value.filter(u => u.ativo !== false).length, // Assume 'ativo' se não existir o campo
    inativos: usuarios.value.filter(u => u.ativo === false).length
  };
});

onMounted(buscarUsuarios);

async function buscarUsuarios() {
  carregando.value = true;
  try {
    const response = await api.get('/api/admin/usuarios'); // AdminController.java
    usuarios.value = response.data;
  } catch (e) {
    console.error("Erro ao listar usuários", e);
  } finally {
    carregando.value = false;
  }
}

async function excluirUsuario(id) {
  if (!confirm("Tem certeza que deseja remover este usuário permanentemente?")) return;
  try {
    await api.delete(`/api/admin/usuarios/${id}`);
    usuarios.value = usuarios.value.filter(u => u.id !== id);
    feedback.value = { tipo: 'sucesso', msg: 'Usuário removido!' };
  } catch (e) {
    feedback.value = { tipo: 'erro', msg: 'Erro ao excluir usuário.' };
  }
}

async function alternarStatus(usuario) {
  try {
    const novoStatus = !usuario.ativo;
    await api.patch(`/api/admin/usuarios/${usuario.id}/status`, { ativo: novoStatus });
    usuario.ativo = novoStatus;
  } catch (e) {
    alert("Erro ao mudar status.");
  }
}

function abrirEdicao(usuario) {
  usuarioSelecionado.value = { ...usuario };
  mostrarModalEdicao.ref = true;
}

async function salvarEdicao() {
  try {
    await api.put(`/api/usuarios/${usuarioSelecionado.value.id}`, usuarioSelecionado.value); // UsuarioService.java
    buscarUsuarios();
    mostrarModalEdicao.value = false;
    feedback.value = { tipo: 'sucesso', msg: 'Dados atualizados!' };
  } catch (e) {
    alert("Erro ao salvar.");
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0F071D] text-white font-sans pb-10">
    
    <header class="p-4 md:p-6 bg-[#1A1128] border-b border-purple-500/20 sticky top-0 z-30 flex justify-between items-center shadow-lg">
      <div class="flex flex-col">
        <h1 class="text-xl md:text-2xl font-black tracking-tighter">
          SPACE <span class="text-purple-500">ADMIN</span>
        </h1>
        <p class="text-[9px] text-purple-300/40 uppercase tracking-widest font-bold">Painel de Controle</p>
      </div>
      
      <button @click="authStore.logout" class="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 px-4 py-2 rounded-xl transition-all border border-red-500/20 group">
        <span class="text-xs font-black uppercase tracking-widest">Sair</span>
        <span class="group-hover:translate-x-1 transition-transform">🚪</span>
      </button>
    </header>

    <main class="p-4 md:p-8 max-w-7xl mx-auto space-y-8">
      
      <section class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="bg-[#1A1128] p-6 rounded-[30px] border border-purple-500/10 flex flex-col items-center text-center">
          <span class="text-[10px] text-purple-300/40 font-black uppercase tracking-widest mb-1">Total Tripulantes</span>
          <span class="text-3xl font-black text-white">{{ stats.total }}</span>
        </div>
        <div class="bg-[#1A1128] p-6 rounded-[30px] border border-green-500/10 flex flex-col items-center text-center">
          <span class="text-[10px] text-green-300/40 font-black uppercase tracking-widest mb-1">Usuários Ativos</span>
          <span class="text-3xl font-black text-green-400">{{ stats.ativos }}</span>
        </div>
        <div class="bg-[#1A1128] p-6 rounded-[30px] border border-red-500/10 flex flex-col items-center text-center">
          <span class="text-[10px] text-red-300/40 font-black uppercase tracking-widest mb-1">Em Repouso</span>
          <span class="text-3xl font-black text-red-400">{{ stats.inativos }}</span>
        </div>
      </section>

      <section class="bg-[#1A1128] rounded-[40px] border border-purple-500/10 overflow-hidden shadow-2xl">
        <div class="p-6 border-b border-purple-500/10 flex justify-between items-center bg-purple-900/5">
          <h2 class="text-sm font-black uppercase tracking-widest text-purple-400">Gerenciar Tripulação</h2>
          <button class="bg-purple-600 text-[10px] font-black px-4 py-2 rounded-lg hover:bg-purple-500 transition-all">
            + NOVO
          </button>
        </div>

        <div v-if="carregando" class="p-20 text-center animate-pulse text-purple-300 font-bold uppercase text-xs">Sincronizando banco...</div>
        
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead class="hidden md:table-header-group bg-purple-900/10">
              <tr>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase">Usuário</th>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase">Contato</th>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase text-center">Status</th>
                <th class="p-4 text-[10px] font-black text-purple-300 uppercase text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-purple-500/5">
              <tr v-for="user in usuarios" :key="user.id" class="flex flex-col md:table-row p-4 md:p-0">
                <td class="p-4 flex items-center gap-3">
                  <img :src="user.avatarUrl" class="w-10 h-10 rounded-full border border-purple-500/30">
                  <div class="flex flex-col">
                    <span class="text-sm font-bold">{{ user.nome }}</span>
                    <span class="text-[10px] text-purple-400 font-bold uppercase tracking-tighter">{{ user.role }}</span>
                  </div>
                </td>
                
                <td class="p-4 md:table-cell">
                  <div class="flex flex-col">
                    <span class="text-xs font-medium text-purple-200/70">{{ user.email }}</span>
                    <span class="text-[10px] text-purple-300/40">{{ user.telefone || 'Sem telefone' }}</span>
                  </div>
                </td>

                <td class="p-4 text-center md:table-cell">
                  <button 
                    @click="alternarStatus(user)"
                    :class="['px-3 py-1 rounded-full text-[9px] font-black uppercase transition-all border', 
                      user.ativo !== false ? 'bg-green-500/10 text-green-400 border-green-500/20' : 'bg-red-500/10 text-red-400 border-red-500/20']"
                  >
                    {{ user.ativo !== false ? 'Ativo' : 'Desativado' }}
                  </button>
                </td>

                <td class="p-4 text-right flex md:table-cell gap-2 justify-end">
                  <button @click="abrirEdicao(user)" class="p-2 bg-purple-500/10 text-purple-400 rounded-lg hover:bg-purple-500/20 transition-all">📝</button>
                  <button @click="excluirUsuario(user.id)" class="p-2 bg-red-500/10 text-red-400 rounded-lg hover:bg-red-500/20 transition-all">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <div v-if="mostrarModalEdicao" class="fixed inset-0 bg-black/80 backdrop-blur-md z-50 flex items-center justify-center p-4">
      <div class="bg-[#1A1128] border border-purple-500/30 w-full max-w-md rounded-[30px] p-8 shadow-2xl">
        <h3 class="text-lg font-black text-purple-400 mb-6 uppercase tracking-widest">Editar Tripulante</h3>
        <div class="space-y-4">
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">Nome Completo</label>
            <input v-model="usuarioSelecionado.nome" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm focus:border-purple-500 outline-none">
          </div>
          <div>
            <label class="text-[9px] uppercase font-bold text-purple-300/40 ml-1">E-mail</label>
            <input v-model="usuarioSelecionado.email" class="w-full bg-white/5 border border-purple-500/20 rounded-xl px-4 py-3 text-sm focus:border-purple-500 outline-none">
          </div>
          <div class="flex gap-3 pt-4">
            <button @click="mostrarModalEdicao = false" class="flex-1 py-3 bg-white/5 rounded-xl text-xs font-bold uppercase hover:bg-white/10 transition-all">Cancelar</button>
            <button @click="salvarEdicao" class="flex-1 py-3 bg-purple-600 rounded-xl text-xs font-bold uppercase hover:bg-purple-500 shadow-lg transition-all">Salvar</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Scrollbar Estilizada */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: rgba(139, 92, 246, 0.2); border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background: rgba(139, 92, 246, 0.4); }

@media (max-width: 768px) {
  table, thead, tbody, th, td, tr { display: block; }
}
</style>