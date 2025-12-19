<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const usuarios = ref([]);
const carregando = ref(true);

const mostrarModalEdicao = ref(false);
const mostrarModalCadastro = ref(false);
const usuarioSelecionado = ref(null);

const novoUsuario = ref({
  nome: '',
  email: '',
  password: '',
  telefone: '',
  dataValidade: '',
  role: 'USER'
});

const stats = computed(() => {
  return {
    total: usuarios.value.length,
    ativos: usuarios.value.filter(u => u.ativo).length,
    vencidos: usuarios.value.filter(u => !u.ativo).length
  };
});

onMounted(buscarUsuarios);

async function buscarUsuarios() {
  carregando.value = true;
  try {
    const response = await api.get('/api/admin/usuarios');
    usuarios.value = response.data;
  } catch (e) {
    console.error(e);
  } finally {
    carregando.value = false;
  }
}

async function cadastrarUsuario() {
  try {
    // Validade padrão de 30 dias se vazio
    if (!novoUsuario.value.dataValidade) {
        const hoje = new Date();
        hoje.setDate(hoje.getDate() + 30);
        novoUsuario.value.dataValidade = hoje.toISOString().split('T')[0];
    }

    await api.post('/api/admin/profissionais', novoUsuario.value);
    alert("Profissional cadastrado!");
    mostrarModalCadastro.value = false;
    novoUsuario.value = { nome: '', email: '', password: '', telefone: '', dataValidade: '', role: 'USER' };
    buscarUsuarios();
  } catch (e) {
    alert(e.response?.data?.message || "Erro ao cadastrar.");
  }
}

async function excluirUsuario(usuario) {
  // BLOQUEIO: Não excluir admins
  if (usuario.role === 'ADMIN') {
    return alert("⚠️ Segurança: Não é possível excluir um Administrador.");
  }
  
  if (!confirm(`Tem certeza que deseja excluir o acesso de ${usuario.nome}?`)) return;
  
  try {
    await api.delete(`/api/admin/usuarios/${usuario.id}`);
    usuarios.value = usuarios.value.filter(u => u.id !== usuario.id);
  } catch (e) { alert("Erro ao excluir."); }
}

async function alternarStatus(usuario) {
  if (usuario.role === 'ADMIN') return; 
  try {
    await api.patch(`/api/admin/usuarios/${usuario.id}/status`);
    usuario.ativo = !usuario.ativo;
  } catch (e) { alert("Erro ao mudar status."); }
}

function abrirEdicao(usuario) {
  usuarioSelecionado.value = { ...usuario, dataValidade: usuario.dataValidade ? usuario.dataValidade.split('T')[0] : '' };
  mostrarModalEdicao.value = true;
}

async function salvarEdicao() {
  try {
    await api.put(`/api/admin/usuarios/${usuarioSelecionado.value.id}`, usuarioSelecionado.value);
    buscarUsuarios();
    mostrarModalEdicao.value = false;
    alert("Dados atualizados!");
  } catch (e) { alert("Erro ao salvar."); }
}

function formatarData(data) {
  if (!data) return 'Vitalício';
  return new Date(data).toLocaleDateString('pt-BR');
}
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] font-sans pb-10">
    
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30 px-6 py-4 flex justify-between items-center shadow-sm">
      <div class="flex items-center gap-2">
        <h1 class="text-xl font-bold text-[#0F172A]">Painel <span class="text-[#DB2777]">Admin</span></h1>
      </div>
      <button @click="authStore.logout" class="text-xs font-bold text-red-500 bg-red-50 px-4 py-2 rounded-lg hover:bg-red-100 transition-colors">SAIR</button>
    </header>

    <main class="p-6 max-w-7xl mx-auto space-y-8">
      
      <section class="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-gray-400 uppercase">Total Usuários</span>
          <p class="text-3xl font-bold text-[#0F172A] mt-1">{{ stats.total }}</p>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-green-600 uppercase">Ativos</span>
          <p class="text-3xl font-bold text-green-600 mt-1">{{ stats.ativos }}</p>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-red-500 uppercase">Suspensos</span>
          <p class="text-3xl font-bold text-red-500 mt-1">{{ stats.vencidos }}</p>
        </div>
      </section>

      <section class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center flex-wrap gap-4">
          <h2 class="font-bold text-[#0F172A]">Gerenciar Profissionais</h2>
          <button @click="mostrarModalCadastro = true" class="bg-[#DB2777] text-white text-xs font-bold px-6 py-3 rounded-xl hover:brightness-110 transition-all shadow-lg shadow-pink-500/30">
            + NOVO PROFISSIONAL
          </button>
        </div>

        <div v-if="carregando" class="p-12 text-center text-gray-400">Carregando dados...</div>
        
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="p-4 text-xs font-bold text-gray-500 uppercase">Usuário</th>
                <th class="p-4 text-xs font-bold text-gray-500 uppercase">Validade</th>
                <th class="p-4 text-xs font-bold text-gray-500 uppercase text-center">Status</th>
                <th class="p-4 text-xs font-bold text-gray-500 uppercase text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="user in usuarios" :key="user.id" class="hover:bg-gray-50/50 transition-colors">
                <td class="p-4">
                  <div class="flex flex-col">
                    <span class="text-sm font-bold text-[#0F172A] flex items-center gap-2">
                      {{ user.nome }}
                      <span v-if="user.role === 'ADMIN'" class="bg-purple-100 text-purple-700 text-[10px] px-2 py-0.5 rounded-full">ADMIN</span>
                    </span>
                    <span class="text-xs text-gray-400">{{ user.email }}</span>
                  </div>
                </td>
                
                <td class="p-4 text-xs font-medium text-gray-600">
                   <span v-if="user.role === 'ADMIN'">---</span>
                   <span v-else :class="{'text-red-500 font-bold': !user.ativo}">{{ formatarData(user.dataValidade) }}</span>
                </td>

                <td class="p-4 text-center">
                  <button 
                    @click="alternarStatus(user)"
                    :disabled="user.role === 'ADMIN'"
                    :class="['px-3 py-1 rounded-full text-[10px] font-bold uppercase border transition-all', 
                      user.ativo ? 'bg-green-50 text-green-600 border-green-100' : 'bg-red-50 text-red-500 border-red-100',
                      user.role === 'ADMIN' ? 'opacity-50 cursor-not-allowed' : 'hover:scale-105']"
                  >
                    {{ user.ativo ? 'Ativo' : 'Suspenso' }}
                  </button>
                </td>

                <td class="p-4 text-right">
                  <div class="flex gap-2 justify-end">
                    <button @click="abrirEdicao(user)" class="p-2 bg-gray-100 rounded-lg text-gray-600 hover:text-[#DB2777] transition-colors" title="Editar">✏️</button>
                    <button 
                      v-if="user.role !== 'ADMIN'"
                      @click="excluirUsuario(user)" 
                      class="p-2 bg-red-50 rounded-lg text-red-500 hover:bg-red-100 transition-colors" title="Excluir"
                    >🗑️</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <div v-if="mostrarModalCadastro" class="fixed inset-0 bg-[#0F172A]/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white w-full max-w-md rounded-3xl p-8 shadow-2xl">
        <h3 class="text-lg font-bold text-[#0F172A] mb-6">Cadastrar Profissional</h3>
        <div class="space-y-4">
          <input v-model="novoUsuario.nome" class="input-modern" placeholder="Nome Completo">
          <input v-model="novoUsuario.email" type="email" class="input-modern" placeholder="E-mail de Login">
          <input v-model="novoUsuario.telefone" class="input-modern" placeholder="WhatsApp">
          <input v-model="novoUsuario.password" type="password" class="input-modern" placeholder="Senha Inicial">
          <div>
            <label class="text-xs font-bold text-gray-400 ml-1 mb-1 block">Validade da Assinatura</label>
            <input type="date" v-model="novoUsuario.dataValidade" class="input-modern">
          </div>
          
          <div class="flex gap-3 pt-4">
            <button @click="mostrarModalCadastro = false" class="btn-secondary">Cancelar</button>
            <button @click="cadastrarUsuario" class="btn-primary">Cadastrar</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="mostrarModalEdicao" class="fixed inset-0 bg-[#0F172A]/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white w-full max-w-md rounded-3xl p-8 shadow-2xl">
        <h3 class="text-lg font-bold text-[#0F172A] mb-6">Editar Acesso</h3>
        <div class="space-y-4">
          <div class="text-xs text-gray-400 font-bold uppercase mb-1">Dados Básicos</div>
          <input v-model="usuarioSelecionado.nome" class="input-modern" placeholder="Nome">
          
          <div v-if="usuarioSelecionado.role !== 'ADMIN'">
             <div class="text-xs text-gray-400 font-bold uppercase mt-4 mb-1">Renovação de Assinatura</div>
             <input type="date" v-model="usuarioSelecionado.dataValidade" class="input-modern">
          </div>
          <div v-else class="bg-purple-50 text-purple-600 p-3 rounded-xl text-xs font-bold text-center">
             Este usuário é Administrador (Acesso Vitalício)
          </div>

          <div class="flex gap-3 pt-4">
            <button @click="mostrarModalEdicao = false" class="btn-secondary">Cancelar</button>
            <button @click="salvarEdicao" class="btn-primary">Salvar Alterações</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.input-modern { @apply w-full px-4 py-3 rounded-xl bg-gray-50 border-none outline-none focus:ring-2 focus:ring-[#DB2777]/50 text-gray-800 text-sm; }
.btn-primary { @apply flex-1 py-3 bg-[#DB2777] text-white rounded-xl text-xs font-bold hover:brightness-110 transition-colors uppercase tracking-wide; }
.btn-secondary { @apply flex-1 py-3 bg-gray-100 text-gray-500 rounded-xl text-xs font-bold hover:bg-gray-200 transition-colors uppercase tracking-wide; }
</style>