<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
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

// Campos de Backup (Correção)
const alertaBackup = ref(false);
const ultimoBackup = ref(null);
const diasDesdeBackup = ref(0);

const stats = computed(() => {
  return {
    total: usuarios.value.length,
    ativos: usuarios.value.filter(u => u.ativo).length,
    vencidos: usuarios.value.filter(u => !u.ativo).length,
    online: usuarios.value.filter(u => u.online).length
  };
});

// --- FUNÇÃO DE DATA (ANTI-FUSO HORÁRIO) ---
function criarDataLocal(dataString) {
  if (!dataString) return null;
  const limpa = dataString.split('T')[0];
  const [ano, mes, dia] = limpa.split('-').map(Number);
  return new Date(ano, mes - 1, dia, 12, 0, 0);
}

// --- LÓGICA DO SININHO (CORRIGIDA) ---
const usuariosVencendo = computed(() => {
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);

  const diasAlerta = 5; 
  const dataLimite = new Date(hoje);
  dataLimite.setDate(hoje.getDate() + diasAlerta);
  dataLimite.setHours(23, 59, 59, 999);

  return usuarios.value.filter(u => {
    // Ignora Admins, sem validade ou INATIVOS (quem já tá bloqueado não precisa avisar)
    if (u.role === 'ADMIN' || !u.dataValidade || !u.ativo) return false;
    
    const validade = criarDataLocal(u.dataValidade);
    if (!validade) return false;

    // CORREÇÃO: Pega qualquer um que a validade seja menor que o limite
    // Isso inclui quem venceu ontem, mês passado (VENCIDOS) e quem vence em 5 dias.
    return validade <= dataLimite;
  });
});

const alertasPendentes = computed(() => usuariosVencendo.value.length);


let pollingInterval = null;
let pingInterval = null;

onMounted(() => {
  // Carrega inicial
  buscarUsuarios();
  
  // Polling silencioso a cada 5s (sem loading spinner)
  pollingInterval = setInterval(syncUsuariosSilent, 5000);
  
  // Mantém Admin online
  pingOnline();
  pingInterval = setInterval(pingOnline, 60000); 
});

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval);
  if (pingInterval) clearInterval(pingInterval);
});

async function pingOnline() {
    try { await api.post('/api/usuarios/ping'); } catch(e) {}
}


async function buscarUsuarios() {
  carregando.value = true;
  await syncUsuariosSilent();
  carregando.value = false;
}

// Busca Silenciosa (Updates)
async function syncUsuariosSilent() {
  try {
    const response = await api.get('/api/admin/usuarios');
    // Só atualiza se o JSON for diferente para evitar flickers
    if (JSON.stringify(usuarios.value) !== JSON.stringify(response.data)) {
        usuarios.value = response.data;
    }
  } catch (e) {
    console.error("Erro sync:", e);
  }
}

async function cadastrarUsuario() {
  try {
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
  let dataPura = '';
  if (usuario.dataValidade) {
      dataPura = usuario.dataValidade.split('T')[0];
  }
  // Inicializa novaSenha vazio para não enviar se não for alterar
  usuarioSelecionado.value = { ...usuario, dataValidade: dataPura, novaSenha: '' };
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
  const d = criarDataLocal(data);
  if (!d) return 'Vitalício';
  return d.toLocaleDateString('pt-BR');
}

function calcularDiasRestantes(data) {
    if (!data) return null;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const validade = criarDataLocal(data);
    if (!validade) return null;
    
    // Diferença em ms
    const diff = validade - hoje;
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

function adicionarDiasValidade(dias) {
    let base = new Date();
    // Se já tem data válida futura, adiciona a partir dela. Se não, a partir de hoje.
    if (usuarioSelecionado.value.dataValidade) {
        const atual = criarDataLocal(usuarioSelecionado.value.dataValidade);
        if (atual && atual > base) {
            base = atual;
        }
    }
    
    base.setDate(base.getDate() + dias);
    usuarioSelecionado.value.dataValidade = base.toISOString().split('T')[0];
}

// Verifica se já venceu (para exibição no template)
function isVencido(usuario) {
    if (!usuario.dataValidade) return false;
    const hoje = new Date();
    hoje.setHours(0,0,0,0);
    const validade = criarDataLocal(usuario.dataValidade);
    return validade < hoje;
}

function isVencendo(usuario) {
  if (usuario.role === 'ADMIN' || !usuario.dataValidade) return false;
  const hoje = new Date();
  hoje.setHours(0,0,0,0);
  const dataLimite = new Date(hoje);
  dataLimite.setDate(hoje.getDate() + 5);
  
  const validade = criarDataLocal(usuario.dataValidade);
  if (!validade) return false;

  // Retorna true se estiver no intervalo de hoje até 5 dias
  return validade >= hoje && validade <= dataLimite;
}

async function fazerBackup() {
    try {
        const response = await api.post('/api/admin/backup', {}, { responseType: 'blob' });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'backup_spacenails.sql');
        document.body.appendChild(link);
        link.click();
        link.remove();
        alert("Backup baixado com sucesso! Guarde este arquivo em segurança.");
    } catch(e) {
        console.error(e);
        alert("Erro ao gerar backup.");
    }
}

// LÓGICA DE RESTORE
// LÓGICA DE RESTORE
const backupInput = ref(null);

function triggerRestore() {
    if(backupInput.value) backupInput.value.click();
}

async function processarRestore(e) {
    const file = e.target.files[0];
    if (!file) return;

    if (!confirm("⚠️ ATENÇÃO: Isso irá APAGAR TODOS os dados atuais e restaurar os do backup. Deseja continuar?")) {
        e.target.value = ''; // Limpa
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        carregando.value = true;
        const response = await api.post('/api/admin/restore', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        // Show detailed report from server
        alert(response.data); 
        
        window.location.reload();
    } catch (err) {
        console.error(err);
        alert("Erro ao restaurar: " + (err.response?.data?.message || err.message));
        carregando.value = false;
    }
}
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] font-sans pb-10">
    
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30 px-4 md:px-6 py-4 flex justify-between items-center shadow-sm">
      <div class="flex items-center gap-2">
        <img src="/icon.png" alt="Icon" class="w-8 h-8 animate-bounce-slow" />
        <h1 class="text-lg md:text-xl font-bold text-[#0F172A]">Painel <span class="text-[#DB2777]">Admin</span></h1>
      </div>

      <div class="flex items-center gap-3 md:gap-4">
        
        <div class="relative group cursor-pointer" title="Alertas de Assinatura">
          <div class="p-2 rounded-xl hover:bg-gray-100 transition-colors relative">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-gray-600">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            
            <span v-if="alertasPendentes > 0" class="absolute -top-1 -right-1 min-w-[18px] h-[18px] bg-red-500 border-2 border-white rounded-full flex items-center justify-center text-[9px] font-bold text-white animate-pulse">
              {{ alertasPendentes }}
            </span>
          </div>

          <div v-if="alertasPendentes > 0" class="absolute right-0 top-full mt-2 w-72 bg-white shadow-xl rounded-xl p-4 border border-gray-100 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50">
             <div class="flex items-center gap-2 mb-2 pb-2 border-b border-gray-100">
                <span class="w-2 h-2 rounded-full bg-red-500"></span>
                <p class="text-xs font-bold text-gray-900 uppercase">Atenção (Vencidos ou Próximos)</p>
             </div>
             <ul class="space-y-2 max-h-48 overflow-y-auto custom-scrollbar">
               <li v-for="u in usuariosVencendo" :key="u.id" class="text-xs text-gray-600 flex justify-between items-center p-1 hover:bg-gray-50 rounded">
                 <span class="font-medium text-[#0F172A] truncate w-32">{{ u.nome }}</span>
                 
                 <span v-if="isVencido(u)" class="font-bold text-white bg-red-500 px-2 py-0.5 rounded ml-2 text-[10px]">VENCIDO</span>
                 <span v-else class="font-bold text-orange-500 bg-orange-50 px-2 py-0.5 rounded ml-2">{{ formatarData(u.dataValidade) }}</span>
               </li>
             </ul>
          </div>
        </div>

        <div class="h-6 w-px bg-gray-200 mx-1"></div>

        <div class="flex items-center gap-4">
            <div v-if="alertaBackup" class="hidden md:flex items-center gap-2 bg-yellow-50 text-yellow-700 px-3 py-1.5 rounded-lg border border-yellow-200 animate-pulse" title="Faça um backup urgente!">
                <span class="text-xs font-bold">⚠️ Backup Pendente ({{ diasDesdeBackup }} dias)</span>
            </div>
            <button @click="authStore.logout" class="text-xs font-bold text-red-500 bg-red-50 px-4 py-2 rounded-lg hover:bg-red-100 transition-colors">SAIR</button>
        </div>
      </div>
    </header>

    <main class="p-4 md:p-6 max-w-7xl mx-auto space-y-8">
      
      <section class="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-gray-400 uppercase">Total Usuários</span>
          <p class="text-3xl font-bold text-[#0F172A] mt-1">{{ stats.total }}</p>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-green-600 uppercase">Ativos</span>
          <p class="text-3xl font-bold text-green-600 mt-1">{{ stats.ativos }}</p>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-blue-500 uppercase">Online Agora</span>
          <p class="text-3xl font-bold text-blue-500 mt-1 flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-blue-500 animate-pulse"></span>
              {{ stats.online }}
          </p>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <span class="text-xs font-bold text-red-500 uppercase">Inativos/Vencidos</span>
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
                <th class="p-4 text-xs font-bold text-gray-500 uppercase text-center">Cód.</th>
                <th class="p-4 text-xs font-bold text-gray-500 uppercase text-center">Status</th>
                <th class="p-4 text-xs font-bold text-gray-500 uppercase text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="user in usuarios" :key="user.id" class="hover:bg-gray-50/50 transition-colors">
                <td class="p-4">
                  <div class="flex flex-col">
                    <span class="text-sm font-bold text-[#0F172A] flex items-center gap-2">
                      <span v-if="user.online" class="w-2.5 h-2.5 rounded-full bg-green-500 animate-pulse border-2 border-white shadow-sm" title="Online Agora"></span>
                      {{ user.nome }}
                      <span v-if="isVencido(user) && user.ativo" class="bg-red-100 text-red-600 text-[10px] px-2 py-0.5 rounded-full font-bold whitespace-nowrap">VENCIDO</span>
                      <span v-else-if="isVencendo(user) && user.ativo" class="bg-orange-100 text-orange-600 text-[10px] px-2 py-0.5 rounded-full animate-pulse font-bold whitespace-nowrap">VENCE EM BREVE</span>
                      <span v-if="user.role === 'ADMIN'" class="bg-purple-100 text-purple-700 text-[10px] px-2 py-0.5 rounded-full">ADMIN</span>
                    </span>
                    <span class="text-xs text-gray-400">{{ user.email }}</span>
                  </div>
                </td>
                
                <td class="p-4 text-xs font-medium text-gray-600 whitespace-nowrap">
                   <span v-if="user.role === 'ADMIN'">---</span>
                   <span v-else :class="{
                     'text-red-500 font-bold': !user.ativo || isVencido(user),
                     'text-orange-500 font-bold': isVencendo(user)
                   }">
                     {{ formatarData(user.dataValidade) }}
                     <span class="text-[10px] ml-1 opacity-75"
                        :class="calcularDiasRestantes(user.dataValidade) < 0 ? 'text-red-600' : 'text-gray-500'">
                        ({{ calcularDiasRestantes(user.dataValidade) }} dias)
                     </span>
                   </span>
                </td>

                <td class="p-4 text-center">
                   <span class="font-mono text-xs font-bold text-purple-600 bg-purple-50 px-2 py-1 rounded select-all">{{ user.codigoConvite || '---' }}</span>
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


      <!-- SEÇÃO DE BACKUP -->
      <section class="bg-slate-800 rounded-3xl p-8 text-white relative overflow-hidden">
          <div class="absolute right-0 top-0 w-64 h-64 bg-slate-700/50 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
          
          <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
              <div>
                  <h3 class="text-xl font-bold flex items-center gap-2">
                    🛡️ Segurança do Sistema
                    <span v-if="alertaBackup" class="text-[10px] bg-red-500 text-white px-2 py-0.5 rounded-full animate-pulse">Backup Pendente</span>
                  </h3>
                  <p class="text-slate-400 text-sm mt-1 max-w-xl">
                      Faça backups regulares para garantir a segurança dos dados. O arquivo gerado contém todos os usuários, agendamentos e clientes.
                      <br>
                      <span class="text-xs opacity-50 block mt-2">Último backup: {{ ultimoBackup ? formatarData(ultimoBackup) : 'Nunca realizado' }}</span>
                  </p>
              </div>

              <div class="flex gap-4">
                  <input type="file" ref="backupInput" class="hidden" accept=".sql" @change="processarRestore">
                  
                  <button @click="triggerRestore" class="px-6 py-3 bg-slate-700 hover:bg-slate-600 rounded-xl font-bold text-sm transition-colors text-gray-300 border border-slate-600 hover:border-slate-500">
                     📥 Restaurar Dados
                  </button>

                  <button @click="fazerBackup" class="px-6 py-3 bg-blue-600 hover:bg-blue-500 rounded-xl font-bold text-sm transition-all shadow-lg shadow-blue-500/20 hover:scale-105 flex items-center gap-2">
                     💾 Fazer Backup Agora
                  </button>
              </div>
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
          
          <div class="text-xs text-gray-400 font-bold uppercase mt-4 mb-1">Credenciais de Acesso</div>
          <input v-model="usuarioSelecionado.email" type="email" class="input-modern" placeholder="E-mail de Login">
          <input v-model="usuarioSelecionado.novaSenha" type="password" class="input-modern" placeholder="Nova Senha (deixe em branco para manter)">
          
          <div v-if="usuarioSelecionado.role !== 'ADMIN'">
             <div class="text-xs text-gray-400 font-bold uppercase mt-4 mb-1">Renovação de Assinatura</div>
             <input type="date" v-model="usuarioSelecionado.dataValidade" class="input-modern mb-2">
             
             <!-- Botões de Renovação Rápida -->
             <div class="flex gap-2 flex-wrap">
                 <button @click="adicionarDiasValidade(30)" class="px-3 py-1.5 bg-green-50 text-green-700 text-[10px] font-bold rounded-lg border border-green-200 hover:bg-green-100">+30 Dias</button>
                 <button @click="adicionarDiasValidade(60)" class="px-3 py-1.5 bg-green-50 text-green-700 text-[10px] font-bold rounded-lg border border-green-200 hover:bg-green-100">+60 Dias</button>
                 <button @click="adicionarDiasValidade(90)" class="px-3 py-1.5 bg-green-50 text-green-700 text-[10px] font-bold rounded-lg border border-green-200 hover:bg-green-100">+90 Dias</button>
                 <button @click="adicionarDiasValidade(365)" class="px-3 py-1.5 bg-blue-50 text-blue-700 text-[10px] font-bold rounded-lg border border-blue-200 hover:bg-blue-100">+1 Ano</button>
             </div>
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
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: #f1f1f1; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #DB2777; border-radius: 4px; }
</style>