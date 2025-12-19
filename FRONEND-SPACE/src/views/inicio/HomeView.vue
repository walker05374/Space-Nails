<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const hoje = new Date().toLocaleDateString('pt-BR');

// Estados
const agendamentos = ref([]);
const clientes = ref([]);
const carregando = ref(true);
const abaAtiva = ref('agenda'); // Alterna entre 'agenda' e 'clientes' no mobile

onMounted(async () => {
  try {
    const [resA, resC] = await Promise.all([
      api.get('/api/agendamentos'), // AgendamentoController
      api.get('/api/clientes')       // ClienteController
    ]);
    agendamentos.value = resA.data;
    clientes.value = resC.data;
  } catch (e) {
    console.error("Erro ao carregar dados");
  } finally {
    carregando.value = false;
  }
});

async function atualizarStatus(id, novoStatus) {
  try {
    await api.patch(`/api/agendamentos/${id}/status`, { status: novoStatus });
    // Atualiza lista local
    const item = agendamentos.value.find(a => a.id === id);
    if (item) item.status = novoStatus;
  } catch (e) {
    alert("Erro ao atualizar status.");
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0F071D] text-white pb-24 md:pb-8">
    <header class="p-6 flex justify-between items-center bg-[#1A1128] border-b border-purple-500/10">
      <div class="flex items-center gap-3">
        <img :src="auth.user?.avatar" class="w-12 h-12 rounded-full border-2 border-purple-500">
        <div>
          <h1 class="text-lg font-black uppercase tracking-tighter">{{ auth.user?.nome }}</h1>
          <p class="text-[10px] text-purple-400 font-bold uppercase tracking-widest">{{ hoje }}</p>
        </div>
      </div>
      <button @click="auth.logout" class="p-2 bg-red-500/10 text-red-400 rounded-xl text-xs font-bold">SAIR</button>
    </header>

    <div class="grid grid-cols-2 gap-4 p-6">
      <div class="bg-white/5 p-4 rounded-2xl border border-white/5 text-center">
        <p class="text-[10px] text-purple-300/40 uppercase font-bold">Agendados</p>
        <p class="text-2xl font-black text-purple-400">{{ agendamentos.length }}</p>
      </div>
      <div class="bg-white/5 p-4 rounded-2xl border border-white/5 text-center">
        <p class="text-[10px] text-purple-300/40 uppercase font-bold">Clientes</p>
        <p class="text-2xl font-black">{{ clientes.length }}</p>
      </div>
    </div>

    <div class="flex px-6 mb-6 gap-4">
      <button @click="abaAtiva = 'agenda'" :class="['flex-1 py-3 rounded-2xl font-bold text-xs uppercase tracking-widest transition-all', abaAtiva === 'agenda' ? 'bg-purple-600 shadow-lg' : 'bg-white/5']">
        📅 Agenda
      </button>
      <button @click="abaAtiva = 'clientes'" :class="['flex-1 py-3 rounded-2xl font-bold text-xs uppercase tracking-widest transition-all', abaAtiva === 'clientes' ? 'bg-purple-600 shadow-lg' : 'bg-white/5']">
        👥 Clientes
      </button>
    </div>

    <main class="px-6">
      <section v-if="abaAtiva === 'agenda'" class="space-y-4">
        <div v-if="agendamentos.length === 0" class="text-center py-10 opacity-30 text-xs italic">Nenhum atendimento marcado.</div>
        <div v-for="agenda in agendamentos" :key="agenda.id" class="p-5 bg-[#1A1128] rounded-[25px] border border-purple-500/10 shadow-lg">
          <div class="flex justify-between items-start mb-3">
            <div>
              <p class="font-black text-sm uppercase">{{ agenda.clienteNome }}</p>
              <p class="text-[10px] text-purple-400 font-bold">{{ agenda.dataHora }}</p>
            </div>
            <span :class="['text-[8px] px-2 py-1 rounded-md font-black uppercase', agenda.status === 'PENDENTE' ? 'bg-yellow-500/20 text-yellow-400' : 'bg-green-500/20 text-green-400']">
              {{ agenda.status }}
            </span>
          </div>
          <div v-if="agenda.status === 'PENDENTE'" class="flex gap-2 mt-4">
            <button @click="atualizarStatus(agenda.id, 'CONCLUIDO')" class="flex-1 py-2 bg-green-600 rounded-xl text-[9px] font-black uppercase">Concluir</button>
            <button @click="atualizarStatus(agenda.id, 'CANCELADO')" class="flex-1 py-2 bg-red-600/20 text-red-400 rounded-xl text-[9px] font-black uppercase">Cancelar</button>
          </div>
        </div>
      </section>

      <section v-if="abaAtiva === 'clientes'" class="space-y-3">
        <button class="w-full py-4 mb-4 border-2 border-dashed border-purple-500/30 text-purple-400 rounded-2xl text-[10px] font-black uppercase">+ Novo Cliente</button>
        <div v-for="cliente in clientes" :key="cliente.id" class="flex items-center justify-between p-4 bg-[#1A1128] rounded-2xl border border-white/5">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-purple-500/20 rounded-full flex items-center justify-center text-sm">💅</div>
            <div>
              <p class="text-sm font-bold">{{ cliente.nome }}</p>
              <p class="text-[10px] text-purple-300/40">{{ cliente.telefone }}</p>
            </div>
          </div>
          <a :href="'https://wa.me/55' + cliente.telefone" class="text-green-400 text-lg">💬</a>
        </div>
      </section>
    </main>

    <div class="fixed bottom-6 left-1/2 -translate-x-1/2 w-[90%] md:max-w-md">
      <button class="w-full bg-gradient-to-r from-purple-600 to-indigo-600 py-4 rounded-2xl shadow-[0_10px_30px_rgba(139,92,246,0.4)] font-black text-xs tracking-widest flex items-center justify-center gap-2">
        🚀 NOVO AGENDAMENTO
      </button>
    </div>
  </div>
</template>