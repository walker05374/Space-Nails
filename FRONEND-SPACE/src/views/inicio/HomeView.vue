<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const hojeStr = new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });

// --- ESTADOS ---
const abaPrincipal = ref('dashboard'); // 'dashboard' | 'agendamentos' | 'clientes' | 'servicos'

// DATAS PARA O GRÁFICO (Padrão: Últimos 7 dias)
const dataInicioGrafico = ref(new Date(new Date().setDate(new Date().getDate() - 7)).toISOString().split('T')[0]);
const dataFimGrafico = ref(new Date().toISOString().split('T')[0]);

// DATA PARA A LISTA DE AGENDAMENTOS (Padrão: Hoje)
const dataFiltroAgenda = ref(new Date().toISOString().split('T')[0]);

const carregando = ref(true);
const modalAgendaOpen = ref(false);
const modalClienteOpen = ref(false);
const modalServicoOpen = ref(false);

const agendamentos = ref([]);
const clientes = ref([]);
const servicos = ref([]);

// Filtros de Cliente
const termoBuscaCliente = ref('');
const ordemCliente = ref('az'); 

// --- FORMS ---
const novoAgendamento = ref({
  clienteId: '',
  servicoSelecionado: '',
  nomeServicoCustom: '',
  valorCustom: '',
  data: new Date().toISOString().split('T')[0],
  hora: '',
  observacoes: ''
});

const novoCliente = ref({ nome: '', telefone: '', endereco: '' });
const novoServico = ref({ nome: '', valor: '' });

// --- API ---
onMounted(carregarDados);

async function carregarDados() {
  try {
    const [resA, resC, resS] = await Promise.all([
      api.get('/api/agendamentos'),
      api.get('/api/clientes'),
      api.get('/api/servicos').catch(() => ({ data: [] }))
    ]);
    agendamentos.value = resA.data;
    clientes.value = resC.data;
    if (resS.data && resS.data.length > 0) {
      servicos.value = resS.data;
    } else {
      servicos.value = [
        { id: 1, nome: 'Manicure', valor: 35.00 },
        { id: 2, nome: 'Pedicure', valor: 40.00 },
      ];
    }
  } catch (e) {
    console.error("Erro ao carregar", e);
  } finally {
    carregando.value = false;
  }
}

// --- LÓGICA DO DASHBOARD (FINANCEIRO DE HOJE) ---
const financeiroHoje = computed(() => {
  const hojeISO = new Date().toISOString().split('T')[0];
  let recebido = 0;
  let aReceber = 0;
  
  agendamentos.value.forEach(a => {
    if (a.dataHora.startsWith(hojeISO)) {
      if (a.status === 'CONCLUIDO') {
        recebido += a.valorServico;
      } else if (a.status === 'PENDENTE' || a.status === 'CONFIRMADO') {
        aReceber += a.valorServico;
      }
    }
  });

  return { recebido, aReceber, total: recebido + aReceber };
});

// --- LÓGICA DO GRÁFICO (PERÍODO PERSONALIZADO) ---
const dadosGrafico = computed(() => {
  if (!dataInicioGrafico.value || !dataFimGrafico.value) return [];

  // Filtra agendamentos CONCLUÍDOS dentro do período selecionado
  const itensNoPeriodo = agendamentos.value.filter(a => {
    const dataItem = a.dataHora.split('T')[0];
    return a.status === 'CONCLUIDO' && dataItem >= dataInicioGrafico.value && dataItem <= dataFimGrafico.value;
  });

  // Agrupa por dia
  const mapaDias = {};
  itensNoPeriodo.forEach(a => {
    const dia = a.dataHora.split('T')[0]; 
    const diaFormatado = new Date(a.dataHora).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
    
    if (!mapaDias[dia]) {
      mapaDias[dia] = { dia: diaFormatado, valor: 0, rawDate: dia };
    }
    mapaDias[dia].valor += a.valorServico;
  });

  // Ordena e formata para o gráfico
  const dados = Object.values(mapaDias).sort((a, b) => a.rawDate.localeCompare(b.rawDate));
  
  // Normaliza altura das barras (0 a 100%)
  const maiorValor = Math.max(...dados.map(d => d.valor), 1);
  dados.forEach(d => d.percentual = (d.valor / maiorValor) * 100);

  return dados;
});

const totalPeriodoGrafico = computed(() => {
  return dadosGrafico.value.reduce((acc, curr) => acc + curr.valor, 0);
});

// --- LÓGICA DA ABA AGENDAMENTOS (PENDENTES vs CONCLUÍDOS) ---
const agendaDoDia = computed(() => {
  // Filtra pela data selecionada na aba de Agendamentos
  return agendamentos.value.filter(a => a.dataHora.startsWith(dataFiltroAgenda.value));
});

const listaPendentes = computed(() => {
  return agendaDoDia.value
    .filter(a => a.status === 'PENDENTE' || a.status === 'CONFIRMADO')
    .sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora));
});

const listaConcluidos = computed(() => {
  return agendaDoDia.value
    .filter(a => a.status === 'CONCLUIDO')
    .sort((a, b) => new Date(a.dataHora) - new Date(b.dataHora));
});

// --- LÓGICA DE CLIENTES ---
const clientesFiltrados = computed(() => {
  let lista = [...clientes.value];
  if (termoBuscaCliente.value) {
    const termo = termoBuscaCliente.value.toLowerCase();
    lista = lista.filter(c => c.nome.toLowerCase().includes(termo) || c.telefone.includes(termo));
  }
  if (ordemCliente.value === 'az') lista.sort((a, b) => a.nome.localeCompare(b.nome));
  else if (ordemCliente.value === 'recentes') lista.sort((a, b) => b.id - a.id);
  return lista;
});

// --- FUNÇÕES AUXILIARES ---
const valorTotalNovoAgendamento = computed(() => {
  if (novoAgendamento.value.servicoSelecionado === 'custom') {
    return parseFloat(novoAgendamento.value.valorCustom) || 0;
  }
  const s = servicos.value.find(s => s.id === novoAgendamento.value.servicoSelecionado);
  return s ? s.valor : 0;
});

async function salvarAgendamento() {
  if(!novoAgendamento.value.clienteId || !novoAgendamento.value.data || !novoAgendamento.value.hora) {
    return alert("Preencha cliente, data e hora.");
  }
  
  let nomeServicoFinal = '';
  let valorFinal = 0;
  let servicoIdFinal = null;

  if (novoAgendamento.value.servicoSelecionado === 'custom') {
    nomeServicoFinal = novoAgendamento.value.nomeServicoCustom;
    valorFinal = parseFloat(novoAgendamento.value.valorCustom);
  } else {
    const s = servicos.value.find(item => item.id === novoAgendamento.value.servicoSelecionado);
    if (s) {
      nomeServicoFinal = s.nome;
      valorFinal = s.valor;
      servicoIdFinal = s.id;
    }
  }

  try {
    const payload = {
      clienteId: parseInt(novoAgendamento.value.clienteId),
      servicoId: servicoIdFinal,
      nomeServico: nomeServicoFinal,
      valorServico: valorFinal,
      dataHora: `${novoAgendamento.value.data}T${novoAgendamento.value.hora}:00`,
      observacoes: novoAgendamento.value.observacoes || ''
    };

    await api.post('/api/agendamentos', payload);
    modalAgendaOpen.value = false;
    carregarDados();
    alert("Agendamento realizado!");
    novoAgendamento.value = { clienteId: '', servicoSelecionado: '', nomeServicoCustom: '', valorCustom: '', data: new Date().toISOString().split('T')[0], hora: '', observacoes: '' };
  } catch (e) {
    console.error(e);
    alert("Erro ao agendar.");
  }
}

async function salvarCliente() {
    try {
        await api.post('/api/clientes', { ...novoCliente.value, observacoes: novoCliente.value.endereco });
        modalClienteOpen.value = false;
        carregarDados();
        novoCliente.value = { nome: '', telefone: '', endereco: '' };
    } catch(e) { alert("Erro ao salvar cliente."); }
}

async function atualizarStatus(id, status) {
  try {
    await api.patch(`/api/agendamentos/${id}/status`, { status });
    const item = agendamentos.value.find(a => a.id === id);
    if (item) item.status = status;
  } catch (e) { alert("Erro ao atualizar."); }
}

function adicionarServicoLista() {
  if(!novoServico.value.nome || !novoServico.value.valor) return;
  servicos.value.push({ id: Date.now(), nome: novoServico.value.nome, valor: parseFloat(novoServico.value.valor) });
  novoServico.value = { nome: '', valor: '' };
  modalServicoOpen.value = false;
}

function removerServico(index) { servicos.value.splice(index, 1); }

function formatarMoeda(valor) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor || 0);
}
</script>

<template>
  <div class="min-h-screen bg-[#F8FAFC] pb-24 md:pb-10 font-sans">
    
    <header class="bg-white sticky top-0 z-30 shadow-sm px-6 py-4 flex justify-between items-center">
      <div>
        <h1 class="text-xl font-bold text-[#0F172A]">Studio<span class="text-[#DB2777]">.Nails</span></h1>
        <p class="text-xs font-medium text-gray-400 capitalize">{{ hojeStr }}</p>
      </div>
      <div class="flex items-center gap-3">
        <span class="text-sm font-bold text-[#0F172A] hidden md:block">{{ auth.user?.nome }}</span>
        <button @click="auth.logout" class="text-xs text-red-500 font-bold border border-red-100 px-3 py-1.5 rounded-lg hover:bg-red-50">SAIR</button>
      </div>
    </header>

    <main class="max-w-6xl mx-auto px-4 pt-6 space-y-6">
      
      <div class="bg-white p-1.5 rounded-2xl shadow-sm inline-flex w-full md:w-auto border border-gray-100 mb-2 overflow-x-auto">
        <button @click="abaPrincipal = 'dashboard'" :class="['flex-1 md:w-32 py-2 px-4 rounded-xl text-sm font-bold transition-all whitespace-nowrap', abaPrincipal === 'dashboard' ? 'bg-[#0F172A] text-white shadow-md' : 'text-gray-400 hover:bg-gray-50']">Dashboard</button>
        <button @click="abaPrincipal = 'agendamentos'" :class="['flex-1 md:w-32 py-2 px-4 rounded-xl text-sm font-bold transition-all whitespace-nowrap', abaPrincipal === 'agendamentos' ? 'bg-[#0F172A] text-white shadow-md' : 'text-gray-400 hover:bg-gray-50']">Agendamentos</button>
        <button @click="abaPrincipal = 'clientes'" :class="['flex-1 md:w-32 py-2 px-4 rounded-xl text-sm font-bold transition-all whitespace-nowrap', abaPrincipal === 'clientes' ? 'bg-[#0F172A] text-white shadow-md' : 'text-gray-400 hover:bg-gray-50']">Clientes</button>
        <button @click="abaPrincipal = 'servicos'" :class="['flex-1 md:w-32 py-2 px-4 rounded-xl text-sm font-bold transition-all whitespace-nowrap', abaPrincipal === 'servicos' ? 'bg-[#0F172A] text-white shadow-md' : 'text-gray-400 hover:bg-gray-50']">Serviços</button>
      </div>

      <div v-if="abaPrincipal === 'dashboard'" class="space-y-6 animate-fade-in">
        
        <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div class="bg-white p-5 rounded-3xl shadow-sm border border-gray-100 relative overflow-hidden">
            <div class="absolute top-0 right-0 w-16 h-16 bg-green-50 rounded-bl-full -mr-2 -mt-2"></div>
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wider relative z-10">Recebido (Hoje)</p>
            <p class="text-2xl font-bold text-green-600 mt-1 relative z-10">{{ formatarMoeda(financeiroHoje.recebido) }}</p>
          </div>

          <div class="bg-white p-5 rounded-3xl shadow-sm border border-gray-100 relative overflow-hidden">
            <div class="absolute top-0 right-0 w-16 h-16 bg-yellow-50 rounded-bl-full -mr-2 -mt-2"></div>
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wider relative z-10">A Receber (Hoje)</p>
            <p class="text-2xl font-bold text-yellow-600 mt-1 relative z-10">{{ formatarMoeda(financeiroHoje.aReceber) }}</p>
          </div>

          <div class="bg-[#0F172A] p-5 rounded-3xl shadow-lg col-span-2 md:col-span-1 text-white relative overflow-hidden">
            <div class="absolute -bottom-4 -right-4 w-24 h-24 bg-[#DB2777] rounded-full opacity-20 blur-xl"></div>
            <p class="text-xs font-bold text-gray-300 uppercase tracking-wider">Total (Hoje)</p>
            <p class="text-2xl font-bold mt-1">{{ formatarMoeda(financeiroHoje.total) }}</p>
          </div>
        </div>

        <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
          <div class="flex flex-col md:flex-row justify-between items-center mb-6 gap-4">
            <div>
              <h2 class="font-bold text-[#0F172A]">Rendimento por Período</h2>
              <p class="text-xs text-gray-400">Total no período: <span class="text-[#DB2777] font-bold">{{ formatarMoeda(totalPeriodoGrafico) }}</span></p>
            </div>
            
            <div class="flex gap-2 bg-gray-50 p-2 rounded-xl">
               <div>
                  <label class="text-[10px] font-bold text-gray-400 uppercase px-1">Início</label>
                  <input type="date" v-model="dataInicioGrafico" class="bg-white border-none rounded-lg p-1 text-xs font-bold text-gray-600 outline-none">
               </div>
               <div>
                  <label class="text-[10px] font-bold text-gray-400 uppercase px-1">Fim</label>
                  <input type="date" v-model="dataFimGrafico" class="bg-white border-none rounded-lg p-1 text-xs font-bold text-gray-600 outline-none">
               </div>
            </div>
          </div>

          <div class="h-40 flex items-end justify-between gap-2 border-b border-gray-100 pb-2">
             <div v-if="dadosGrafico.length === 0" class="w-full text-center text-gray-300 text-xs py-10">
                Selecione um período com atendimentos concluídos.
             </div>

             <div v-for="(dado, index) in dadosGrafico" :key="index" class="flex flex-col items-center flex-1 group relative">
                <div class="absolute -top-8 bg-[#0F172A] text-white text-[10px] px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap z-10">
                   {{ formatarMoeda(dado.valor) }}
                </div>
                <div class="w-full bg-[#DB2777]/20 rounded-t-md hover:bg-[#DB2777] transition-all duration-300 relative" :style="{ height: (dado.percentual || 5) + '%' }">
                   <div class="absolute top-0 w-full h-1 bg-[#DB2777] rounded-t-md opacity-50"></div>
                </div>
                <span class="text-[10px] text-gray-400 mt-2 font-medium">{{ dado.dia }}</span>
             </div>
          </div>
        </div>
      </div>

      <div v-if="abaPrincipal === 'agendamentos'" class="space-y-6 animate-fade-in">
        
        <div class="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex items-center justify-between">
           <span class="font-bold text-[#0F172A]">Agenda do Dia</span>
           <input type="date" v-model="dataFiltroAgenda" class="bg-gray-50 border-none rounded-xl px-4 py-2 text-sm font-bold text-[#DB2777] outline-none">
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          
          <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
             <h3 class="font-bold text-[#0F172A] mb-4 flex items-center gap-2">
               <span class="w-2 h-2 rounded-full bg-yellow-400"></span> Pendentes / A Fazer
             </h3>
             
             <div v-if="listaPendentes.length === 0" class="text-center py-8 text-gray-400 text-sm">
               Nada pendente para hoje.
             </div>

             <div v-for="agenda in listaPendentes" :key="agenda.id" class="mb-4 pb-4 border-b border-gray-50 last:border-0">
                <div class="flex justify-between items-start">
                  <div>
                    <span class="text-xs font-bold bg-yellow-50 text-yellow-600 px-2 py-0.5 rounded-lg mb-1 inline-block">
                      {{ new Date(agenda.dataHora).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}
                    </span>
                    <h4 class="font-bold text-[#0F172A]">{{ agenda.nomeCliente }}</h4>
                    <p class="text-xs text-gray-500">{{ agenda.nomeServico }}</p>
                  </div>
                  <div class="text-right">
                    <p class="font-bold text-[#DB2777] text-sm">{{ formatarMoeda(agenda.valorServico) }}</p>
                    <div class="flex gap-2 mt-2">
                       <button @click="atualizarStatus(agenda.id, 'CONCLUIDO')" class="text-[10px] font-bold bg-green-100 text-green-600 px-2 py-1 rounded hover:bg-green-200">✔ Concluir</button>
                       <button @click="atualizarStatus(agenda.id, 'CANCELADO')" class="text-[10px] font-bold bg-red-50 text-red-500 px-2 py-1 rounded hover:bg-red-100">✕</button>
                    </div>
                  </div>
                </div>
             </div>
          </div>

          <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
             <h3 class="font-bold text-[#0F172A] mb-4 flex items-center gap-2">
               <span class="w-2 h-2 rounded-full bg-green-500"></span> Concluídos
             </h3>

             <div v-if="listaConcluidos.length === 0" class="text-center py-8 text-gray-400 text-sm">
               Nenhum serviço concluído hoje.
             </div>

             <div v-for="agenda in listaConcluidos" :key="agenda.id" class="mb-4 pb-4 border-b border-gray-50 last:border-0 opacity-75 hover:opacity-100 transition-opacity">
                <div class="flex justify-between items-center">
                  <div>
                    <span class="text-xs font-bold bg-green-50 text-green-600 px-2 py-0.5 rounded-lg mb-1 inline-block">
                      {{ new Date(agenda.dataHora).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}
                    </span>
                    <h4 class="font-bold text-[#0F172A] line-through text-gray-400 decoration-gray-400">{{ agenda.nomeCliente }}</h4>
                    <p class="text-xs text-gray-400">{{ agenda.nomeServico }}</p>
                  </div>
                  <div class="text-right">
                    <p class="font-bold text-gray-400 text-sm">{{ formatarMoeda(agenda.valorServico) }}</p>
                    <span class="text-[10px] font-bold text-green-600 uppercase">Feito</span>
                  </div>
                </div>
             </div>
          </div>

        </div>
      </div>

      <div v-if="abaPrincipal === 'clientes'" class="space-y-4 animate-fade-in">
        
        <div class="flex gap-2">
          <input v-model="termoBuscaCliente" placeholder="🔍 Buscar por nome ou telefone..." class="flex-1 input-modern bg-white">
          <select v-model="ordemCliente" class="w-1/3 input-modern bg-white">
            <option value="az">A-Z</option>
            <option value="recentes">Recentes</option>
          </select>
        </div>

        <button @click="modalClienteOpen = true" class="btn-secondary border-dashed border-[#DB2777] text-[#DB2777] bg-pink-50/50">+ Adicionar Nova Cliente</button>

        <div class="grid gap-3">
          <div v-for="cliente in clientesFiltrados" :key="cliente.id" class="bg-white p-4 rounded-2xl border border-gray-100 flex justify-between items-center group">
             <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center font-bold text-gray-400 text-sm">
                   {{ cliente.nome.charAt(0).toUpperCase() }}
                </div>
                <div>
                   <p class="font-bold text-[#0F172A]">{{ cliente.nome }}</p>
                   <p class="text-xs text-gray-500">{{ cliente.telefone }}</p>
                </div>
             </div>
             <a :href="'https://wa.me/55' + cliente.telefone.replace(/\D/g,'')" target="_blank" class="w-10 h-10 bg-green-100 text-green-600 rounded-full flex items-center justify-center hover:scale-110 transition-transform">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16"><path d="M13.601 2.326A7.854 7.854 0 0 0 7.994 0C3.627 0 .068 3.558.064 7.926c0 1.399.366 2.76 1.057 3.965L0 16l4.204-1.102a7.933 7.933 0 0 0 3.79.965h.004c4.368 0 7.926-3.558 7.93-7.93A7.898 7.898 0 0 0 13.6 2.326zM7.994 14.521a6.573 6.573 0 0 1-3.356-.92l-.24-.144-2.494.654.666-2.433-.156-.251a6.56 6.56 0 0 1-1.007-3.505c0-3.626 2.957-6.584 6.591-6.584a6.56 6.56 0 0 1 4.66 1.931 6.557 6.557 0 0 1 1.928 4.66c-.004 3.639-2.961 6.592-6.592 6.592z"/></svg>
             </a>
          </div>
          <div v-if="clientesFiltrados.length === 0" class="text-center text-gray-400 py-4 text-sm">
             Nenhum cliente encontrado.
          </div>
        </div>
      </div>

      <div v-if="abaPrincipal === 'servicos'" class="space-y-4 animate-fade-in">
        <button @click="modalServicoOpen = true" class="btn-secondary border-dashed">+ Novo Serviço</button>
        <div class="bg-white rounded-3xl shadow-sm overflow-hidden border border-gray-100">
           <div v-for="(servico, index) in servicos" :key="servico.id" class="p-4 border-b border-gray-100 flex justify-between items-center last:border-0">
              <span class="font-bold text-[#0F172A]">{{ servico.nome }}</span>
              <div class="flex items-center gap-4">
                 <span class="font-bold text-[#DB2777]">{{ formatarMoeda(servico.valor) }}</span>
                 <button @click="removerServico(index)" class="text-gray-300 hover:text-red-500">🗑️</button>
              </div>
           </div>
        </div>
      </div>

    </main>

    <button @click="modalAgendaOpen = true" class="fixed bottom-6 right-6 w-14 h-14 bg-[#DB2777] text-white rounded-full shadow-lg shadow-pink-500/40 flex items-center justify-center text-2xl hover:scale-110 transition-transform z-40">
       <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
    </button>

    <div v-if="modalAgendaOpen" class="fixed inset-0 bg-[#0F172A]/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white w-full max-w-md rounded-3xl p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
        <h3 class="text-lg font-bold text-[#0F172A] mb-4">Novo Agendamento</h3>
        
        <div class="space-y-4">
          <select v-model="novoAgendamento.clienteId" class="input-modern">
            <option value="" disabled>Selecione a Cliente...</option>
            <option v-for="c in clientes" :value="c.id" :key="c.id">{{ c.nome }}</option>
          </select>

          <div class="grid grid-cols-2 gap-3">
            <input type="date" v-model="novoAgendamento.data" class="input-modern">
            <input type="time" v-model="novoAgendamento.hora" class="input-modern">
          </div>

          <div class="bg-gray-50 p-3 rounded-xl border border-gray-100">
            <label class="text-xs font-bold text-gray-400 uppercase mb-2 block">O que vamos fazer?</label>
            <select v-model="novoAgendamento.servicoSelecionado" class="input-modern bg-white mb-2">
              <option value="" disabled>Escolha um serviço...</option>
              <option v-for="s in servicos" :value="s.id" :key="s.id">{{ s.nome }} - {{ formatarMoeda(s.valor) }}</option>
              <option value="custom" class="font-bold text-[#DB2777]">+ Digitar outro serviço</option>
            </select>

            <div v-if="novoAgendamento.servicoSelecionado === 'custom'" class="space-y-2 mt-2 pt-2 border-t border-gray-200">
              <input v-model="novoAgendamento.nomeServicoCustom" placeholder="Nome do Serviço" class="input-modern bg-white border-pink-200">
              <input v-model="novoAgendamento.valorCustom" type="number" placeholder="Valor (R$)" class="input-modern bg-white border-pink-200">
            </div>
          </div>

          <textarea v-model="novoAgendamento.observacoes" placeholder="Observações..." class="input-modern"></textarea>

          <div class="bg-[#0F172A] p-4 rounded-xl flex justify-between items-center text-white">
            <span class="text-xs font-bold uppercase text-gray-400">Total</span>
            <span class="text-xl font-bold">{{ formatarMoeda(valorTotalNovoAgendamento) }}</span>
          </div>

          <div class="flex gap-3 pt-2">
            <button @click="modalAgendaOpen = false" class="btn-secondary">Cancelar</button>
            <button @click="salvarAgendamento" class="btn-primary">Confirmar</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="modalClienteOpen" class="fixed inset-0 bg-[#0F172A]/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
        <div class="bg-white w-full max-w-sm rounded-3xl p-6 shadow-2xl space-y-4">
            <h3 class="font-bold text-[#0F172A]">Nova Cliente</h3>
            <input v-model="novoCliente.nome" placeholder="Nome" class="input-modern">
            <input v-model="novoCliente.telefone" placeholder="Telefone" class="input-modern">
            <input v-model="novoCliente.endereco" placeholder="Endereço" class="input-modern">
            <button @click="salvarCliente" class="btn-primary">Salvar</button>
            <button @click="modalClienteOpen = false" class="text-xs font-bold text-gray-400 w-full text-center">Cancelar</button>
        </div>
    </div>

    <div v-if="modalServicoOpen" class="fixed inset-0 bg-[#0F172A]/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white w-full max-w-sm rounded-3xl p-6 shadow-2xl">
        <h3 class="font-bold text-[#0F172A] mb-4">Adicionar ao Menu</h3>
        <div class="space-y-3">
          <input v-model="novoServico.nome" placeholder="Nome (Ex: Unha de Gel)" class="input-modern">
          <input v-model="novoServico.valor" type="number" placeholder="Valor (Ex: 100.00)" class="input-modern">
          <button @click="adicionarServicoLista" class="btn-primary">Adicionar</button>
          <button @click="modalServicoOpen = false" class="text-xs font-bold text-gray-400 w-full mt-2">Cancelar</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }

.input-modern {
  @apply w-full px-4 py-3 rounded-xl bg-gray-50 border-none outline-none focus:ring-2 focus:ring-[#DB2777]/50 text-gray-800 text-sm;
}
.btn-primary { 
  @apply flex-1 py-3 bg-[#DB2777] text-white rounded-xl text-xs font-bold hover:brightness-110 transition-colors uppercase tracking-wide; 
}
.btn-secondary { 
  @apply flex-1 py-3 bg-gray-100 text-gray-500 rounded-xl text-xs font-bold hover:bg-gray-200 transition-colors uppercase tracking-wide; 
}
</style>