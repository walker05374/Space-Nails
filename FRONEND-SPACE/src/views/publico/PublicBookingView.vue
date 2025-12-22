<template>
  <div class="booking-container bg-booking">
    <!-- Overlay gradiente idêntico ao Login -->
    <div class="overlay-gradient"></div>

    <div class="booking-card">
      <div class="header-section">
        <h2 class="title-brand">Space<span class="text-pink">.Nails</span></h2>
        <p class="subtitle" v-if="nomeProfissional">Agendamento com <strong>{{ nomeProfissional }}</strong></p>
        
        <!-- Localização Header -->
        <!-- Localização Header -->
        <div v-if="enderecoProfissional || localizacaoUrl" class="text-center mb-6 -mt-6 flex flex-col items-center gap-2">
            <a v-if="localizacaoUrl" :href="localizacaoUrl" target="_blank" class="inline-flex items-center gap-1 text-xs font-bold text-[#DB2777] bg-pink-50 px-3 py-1.5 rounded-full hover:bg-pink-100 transition-colors animate-bounce">
                📍 Ver Localização no Maps
            </a>
            <span v-if="enderecoProfissional" class="text-xs text-gray-500 font-medium bg-gray-50 px-3 py-1 rounded-lg border border-gray-100">
                🏠 {{ enderecoProfissional }}
            </span>
        </div>
      </div>

      <!-- Passo 0: Escolha Inicial -->
      <div v-if="step === 0" class="step-content text-center">
         <div class="mb-8">
            <h3 class="text-xl font-bold text-gray-700 mb-2">Bem-vindo(a)!</h3>
            <p class="text-sm text-gray-500">O que você deseja fazer hoje?</p>
         </div>

         <div class="grid gap-4">
             <button @click="iniciarNovoAgendamento" class="btn-primary py-4 text-lg shadow-lg">
                ✨ Novo Agendamento
             </button>
             
             <button @click="step = 10" class="btn-secondary py-4 text-xs font-bold uppercase tracking-widest border-dashed">
                🔄 Remarcar / Gerenciar
             </button>
         </div>
      </div>

       <!-- Passo 10: Buscar Agendamento (Remarcação) -->
      <div v-if="step === 10" class="step-content">
          <h3>Buscar Agendamento</h3>
          <div class="form-group mb-4">
              <label class="label-input">Código do Agendamento</label>
              <input v-model="codigoBusca" type="text" placeholder="Ex: A1B2C3" class="input-modern uppercase text-center tracking-[0.5em] font-mono text-xl" maxlength="10">
              <p class="text-[10px] text-gray-400 mt-2 text-center">O código está no seu comprovante anterior.</p>
          </div>
          
          <div class="actions">
             <button @click="step = 0" class="btn-secondary">Voltar</button>
             <button @click="buscarAgendamento" :disabled="!codigoBusca" class="btn-primary">Buscar</button>
          </div>
      </div>

      <!-- Passo 11: Visualizar Agendamento Antigo -->
      <div v-if="step === 11 && agendamentoEncontrado" class="step-content">
          <div class="bg-gray-50 p-4 rounded-xl mb-6 border border-gray-200">
             <h4 class="font-bold text-[#0F172A] mb-1">Agendamento Atual</h4>
             <p class="text-sm text-gray-600">{{ agendamentoEncontrado.servico.nome }}</p>
             <p class="text-lg font-bold text-[#DB2777] my-2">
                {{ formatarDataBonita(agendamentoEncontrado.dataHora.split('T')[0]) }} às {{ agendamentoEncontrado.dataHora.split('T')[1].substring(0,5) }}
             </p>
             <p class="text-xs text-gray-400">Profissional: {{ agendamentoEncontrado.profissional.nome }}</p>
          </div>

          <div class="actions">
              <button @click="iniciarRemarcacao" class="bg-blue-500 text-white w-full py-3 rounded-xl font-bold hover:bg-blue-600 transition-colors shadow-lg shadow-blue-500/30">
                 📅 Escolher Nova Data
              </button>
          </div>
          <button @click="step = 0" class="btn-secondary mt-3 w-full">Voltar</button>
      </div>

      <!-- Passo 1: Selecionar Serviço -->
      <div v-if="step === 1" class="step-content">
        <h3>{{ modoRemarcacao ? 'Remarcar: ' + agendamentoEncontrado.servico.nome : 'Escolha o Serviço' }}</h3>
        <div v-if="loadingServicos" class="loader">Carregando serviços...</div>
        <div v-else class="services-list">
          <div 
            v-for="servico in servicos" 
            :key="servico.id" 
            class="service-item"
            @click="selecionarServico(servico)"
          >
            <div class="flex flex-col items-start gap-1">
                <span class="service-name">{{ servico.nome }}</span>
                <button @click.stop="abrirGaleria(servico)" class="text-[10px] font-bold text-[#DB2777] bg-pink-50 px-2 py-0.5 rounded hover:bg-pink-100 transition-colors flex items-center gap-1">
                    📷 Ver fotos
                </button>
            </div>
            <span class="service-price">R$ {{ servico.valor }}</span>
          </div>
          <div v-if="servicos.length === 0" class="no-data">Nenhum serviço disponível.</div>
        </div>
      </div>

      <!-- Passo 2: Selecionar Data e Horário -->
      <div v-if="step === 2" class="step-content">
        <h3>Escolha a Data e Horário</h3>
        <div class="date-selection mb-6">
        <label class="label-input">Escolha o Dia</label>
        <input type="date" v-model="dataSelecionada" :min="minData" class="input-modern bg-white border border-pink-100" @change="carregarSlots">
      </div>

      <!-- SÓ MOSTRA SE TIVER DATA -->
      <div v-if="dataSelecionada">
          <div v-if="carregandoSlots" class="text-center py-8 text-pink-500 font-bold animate-pulse">Buscando horários disponíveis...</div>
          
          <div v-else>
            <h3 class="text-sm font-bold text-gray-500 mb-4 uppercase tracking-wide">Horários para {{ formatarDataBonita(dataSelecionada) }}</h3>
            
            <div v-if="slots.length === 0" class="no-slots bg-gray-50 rounded-xl border border-dashed border-gray-300">
               Nenhum horário disponível para esta data. 😔
            </div>

            <div v-else class="slots-grid">
              <button 
                v-for="slot in slots" 
                :key="slot" 
                @click="selecionarSlot(slot)"
                :class="['slot-btn', horaSelecionada === slot ? 'selected' : '']"
              >
                {{ slot }}
              </button>
            </div>
          </div>
      </div>
      <div v-else class="text-center py-10 bg-gray-50 rounded-2xl border border-dashed border-gray-200">
          <span class="text-4xl mb-2 block">📅</span>
          <p class="text-gray-400 font-bold text-sm">Selecione uma data acima para ver os horários.</p>
      </div>
        <div class="actions">
          <button @click="step = 1" class="btn-secondary">Voltar</button>
          <button @click="irParaIdentificacao" :disabled="!horaSelecionada" class="btn-primary">Próximo</button>
        </div>
      </div>

      <!-- Passo 3: Identificação -->
      <div v-if="step === 3" class="step-content">
        <h3>Seus Dados</h3>
        <div class="form-group">
          <label class="label-input">Telefone (WhatsApp)</label>
          <input type="text" v-model="cliente.telefone" placeholder="(00) 00000-0000" class="input-modern" />
          <small class="hint">Usaremos seu número para identificá-lo(a).</small>
        </div>

        <div class="form-group mt-4">
          <label class="label-input">Nome Completo</label>
          <input type="text" v-model="cliente.nome" placeholder="Seu nome" class="input-modern" />
          <small class="hint">Se você já é cliente, manteremos seu nome atual.</small>
        </div>

        <div class="actions">
          <button @click="step = 2" class="btn-secondary">Voltar</button>
          <button @click="confirmarAgendamento" :disabled="!cliente.nome || !cliente.telefone" class="btn-primary">
            Confirmar Agendamento
          </button>
        </div>
      </div>

      <!-- STEP 4: SUCESSO / COMPROVANTE -->
    <div v-if="step === 4" class="text-center animate-fade-in">
       
       <div id="receipt-area" class="bg-gradient-to-b from-white to-gray-50 p-6 rounded-2xl border border-gray-200 shadow-sm relative overflow-hidden mb-6">
           <!-- Recortes de Ticket -->
           <div class="absolute top-0 left-0 w-full h-2 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIxMCIgdmlld0JveD0iMCAwIDIwIDEwIiBmaWxsPSIjZjFmNWY5Ij48Y2lyY2xlIGN4PSIxMCIgY3k9Ii01IiByPSIxMCIvPjwvc3ZnPg==')] bg-repeat-x"></div>
           <div class="absolute bottom-0 left-0 w-full h-2 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIxMCIgdmlld0JveD0iMCAwIDIwIDIwIiBmaWxsPSIjZjFmNWY5Ij48Y2lyY2xlIGN4PSIxMCIgY3k9IjE1IiByPSIxMCIvPjwvc3ZnPg==')] bg-repeat-x"></div>

           <div class="text-4xl mb-4">✨</div>
           <h2 class="text-2xl font-bold text-[#0F172A] mb-1">{{ modoRemarcacao ? 'Remarcado!' : 'Agendado!' }}</h2>
           <p class="text-gray-500 text-xs mb-6">{{ modoRemarcacao ? 'Seu horário foi alterado com sucesso.' : 'Seu horário foi reservado com sucesso.' }}</p>

           <!-- CÓDIGO DO AGENDAMENTO -->
           <div class="bg-gray-100 p-3 rounded-xl border border-dashed border-gray-300 mb-6 text-center">
               <span class="block text-[10px] uppercase font-bold text-gray-400 tracking-wider">Seu Código de Agendamento</span>
               <span class="block text-2xl font-mono font-bold text-[#0F172A] tracking-[0.2em] mt-1">{{ agendamentoEncontrado?.codigo || '---' }}</span>
               <span class="block text-[9px] text-gray-400 mt-1">Guarde este código para remarcar se precisar.</span>
           </div>
           
           <div class="space-y-4 text-left">
               <div class="flex justify-between border-b border-dashed border-gray-200 pb-2">
                   <span class="text-xs font-bold text-gray-400 uppercase">Profissional</span>
                   <span class="text-sm font-bold text-[#0F172A]">{{ nomeProfissional || 'Space Nails' }}</span>
               </div>
               <div class="flex justify-between border-b border-dashed border-gray-200 pb-2">
                   <span class="text-xs font-bold text-gray-400 uppercase">Cliente</span>
                   <span class="text-sm font-bold text-[#0F172A]">{{ cliente.nome }}</span>
               </div>
               <div class="flex justify-between border-b border-dashed border-gray-200 pb-2">
                   <span class="text-xs font-bold text-gray-400 uppercase">Serviço</span>
                   <span class="text-sm font-bold text-[#0F172A]">{{ servicoSelecionado?.nome }}</span>
               </div>
               <div class="flex justify-between border-b border-dashed border-gray-200 pb-2">
                   <span class="text-xs font-bold text-gray-400 uppercase">Data & Hora</span>
                   <span class="text-sm font-bold text-[#DB2777]">{{ formatarDataBonita(dataSelecionada) }} às {{ horaSelecionada }}</span>
               </div>
               <div class="flex justify-between pt-1">
                   <span class="text-xs font-bold text-gray-400 uppercase">Valor</span>
                   <span class="text-lg font-bold text-[#0F172A]">{{ servicoSelecionado?.valor ? servicoSelecionado.valor.toLocaleString('pt-BR', {style:'currency', currency:'BRL'}) : 'R$ 0,00' }}</span>
               </div>
           </div>

           <div class="mt-8 bg-pink-50 rounded-lg p-3">
               <p class="text-[10px] text-pink-600 font-bold leading-tight">Te esperamos! Caso precise cancelar ou remarcar, use seu código no menu inicial.</p>
           </div>
       </div>

       <div class="flex flex-col gap-3 no-print">
           <button @click="imprimirComprovante" class="btn-secondary flex items-center justify-center gap-2 mb-2">
             <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
             Salvar Comprovante
           </button>
           
           <!-- Botão WhatsApp -->
           <a v-if="telefoneProfissional" 
              :href="`https://wa.me/55${telefoneProfissional.replace(/\D/g,'')}?text=${modoRemarcacao 
                  ? `Olá ${nomeProfissional}, remarquei meu agendamento (Cód: *${agendamentoEncontrado?.codigo}*) para dia *${formatarDataBonita(dataSelecionada)}* às *${horaSelecionada}*. Aguardo confirmação!` 
                  : `Olá ${nomeProfissional}, acabei de agendar *${servicoSelecionado?.nome}* para dia *${formatarDataBonita(dataSelecionada)}* às *${horaSelecionada}*. Meu código é *${agendamentoEncontrado?.codigo}*. Aguardo confirmação!`}`"
              target="_blank"
              class="inline-flex items-center justify-center gap-2 bg-[#25D366] text-white px-8 py-4 rounded-xl font-bold shadow-lg shadow-green-500/30 hover:bg-[#128C7E] transition-all transform hover:scale-105 mb-4 no-underline cursor-pointer">
              <span class="text-xl">📱</span> Enviar Confirmação via WhatsApp
           </a>

           <button @click="novoAgendamento" class="btn-secondary bg-gray-50 border-gray-200">Voltar ao Início</button>
       </div>
    </div>

       <!-- Mensagem de Erro -->
       <div v-if="erro" class="error-msg">
        {{ erro }}
      </div>

    </div>

    <!-- MODAL GALERIA -->
    <div v-if="modalGaleriaOpen" class="fixed inset-0 bg-black/80 z-50 flex flex-col items-center justify-center p-4 backdrop-blur-sm animate-fade-in" @click.self="modalGaleriaOpen = false">
        <div class="bg-white w-full max-w-2xl rounded-3xl p-6 shadow-2xl relative max-h-[80vh] flex flex-col">
            <button @click="modalGaleriaOpen = false" class="absolute top-4 right-4 text-gray-400 hover:text-red-500 font-bold bg-gray-50 p-2 rounded-full transition-colors">
                ✕
            </button>
            
            <h3 class="text-lg font-bold text-[#0F172A] mb-4 pr-10">Fotos: <span class="text-[#DB2777]">{{ servicoGaleriaNome }}</span></h3>
            
            <div v-if="carregandoFotos" class="flex-1 flex items-center justify-center min-h-[200px]">
                <span class="text-pink-500 font-bold animate-pulse">Carregando fotos...</span>
            </div>
            
            <div v-else-if="fotosGaleria.length === 0" class="flex-1 flex flex-col items-center justify-center min-h-[200px] text-gray-400">
                <span class="text-4xl mb-2">🖼️</span>
                <p>Nenhuma foto cadastrada para este serviço.</p>
            </div>
            
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 gap-4 overflow-y-auto custom-scrollbar pr-2">
                <div v-for="foto in fotosGaleria" :key="foto.id" class="aspect-square rounded-xl overflow-hidden relative group cursor-zoom-in" @click="registrarVisualizacao(foto)">
                    <img :src="foto.imagemUrl" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" loading="lazy">
                    <div class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
                </div>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const step = ref(0) // Começa no step 0 agora
const servicos = ref([])
const slots = ref([])
const loadingServicos = ref(false)
const carregandoSlots = ref(false)
const erro = ref(null)

const servicoSelecionado = ref(null)
const dataSelecionada = ref('')
const horaSelecionada = ref(null)

const profissionalIdConfirmado = ref(null)

// --- GALERIA ---
const modalGaleriaOpen = ref(false)
const fotosGaleria = ref([])
const carregandoFotos = ref(false)
const servicoGaleriaNome = ref('')

async function abrirGaleria(servico) {
    servicoGaleriaNome.value = servico.nome
    modalGaleriaOpen.value = true
    carregandoFotos.value = true
    fotosGaleria.value = []
    
    try {
        const res = await fetch(`${API_URL}/portfolio/servico/${servico.id}`)
        if(res.ok) {
            fotosGaleria.value = await res.json()
        }
    } catch(e) {
        console.error("Erro ao carregar fotos", e)
    } finally {
        carregandoFotos.value = false
    }
}

async function registrarVisualizacao(foto) {
    // Apenas contabiliza click, pode abrir um lightbox se quiser (por enquanto só track)
    try {
        await fetch(`${API_URL}/portfolio/${foto.id}/click`, { method: 'POST' })
    } catch(e) {}
}

// Estados de Remarcação
const codigoBusca = ref('')
const agendamentoEncontrado = ref(null)
const modoRemarcacao = ref(false)

const cliente = ref({
  nome: '',
  telefone: ''
})

const API_URL = 'http://localhost:8080/api/public'

import { useRoute } from 'vue-router'
const route = useRoute()

const nomeProfissional = ref('')

const telefoneProfissional = ref('') // Novo
const enderecoProfissional = ref('') // Novo
const localizacaoUrl = ref('') // Novo
const minData = ref('') // Renamed from minDate

onMounted(async () => {
  loadingServicos.value = true
  
  // Define data mínima como hoje
  const hoje = new Date()
  minData.value = hoje.toISOString().split('T')[0] // Updated to minData

  try {
    // 1. Pega o slug da URL (ex: /agendar/ana)
    const slug = route.params.slug
    if (!slug) {
       erro.value = "Link inválido. Informe o profissional."
       return
    }

    // 2. Resolve o Slug para pegar o ID real
    const profRes = await fetch(`${API_URL}/profissional/slug/${slug}`)
    if (!profRes.ok) throw new Error("Profissional não encontrado.")
    
    const profissional = await profRes.json()
    // Agora busca detalhes completos (telefone, endereco)
    const infoRes = await fetch(`${API_URL}/profissional/${profissional.id}/info`)
    if(infoRes.ok) {
        const info = await infoRes.json()
        nomeProfissional.value = info.nome
        telefoneProfissional.value = info.telefone
        enderecoProfissional.value = info.endereco
        localizacaoUrl.value = info.localizacaoUrl
    } else {
        nomeProfissional.value = profissional.nome
    }
    
    profissionalIdConfirmado.value = profissional.id 

    // 3. Carrega serviços usando o ID real
    const res = await fetch(`${API_URL}/servicos?profissionalId=${profissional.id}`)
    servicos.value = await res.json()
  } catch (e) {
    erro.value = e.message || "Erro ao carregar dados do profissional."
  } finally {
    loadingServicos.value = false
  }
})

const selecionarServico = (servico) => {
  servicoSelecionado.value = servico
  step.value = 2
}

const carregarSlots = async () => {
  if (!dataSelecionada.value) return
  
  carregandoSlots.value = true // Updated to carregandoSlots
  slots.value = []
  horaSelecionada.value = null // Updated to horaSelecionada
  
  try {
    const res = await fetch(`${API_URL}/slots?servicoId=${servicoSelecionado.value.id}&data=${dataSelecionada.value}&profissionalId=${profissionalIdConfirmado.value}`)
    slots.value = await res.json()
  } catch (e) {
    erro.value = "Erro ao buscar horários."
  } finally {
    carregandoSlots.value = false // Updated to carregandoSlots
  }
}

const iniciarNovoAgendamento = () => {
    step.value = 1
    modoRemarcacao.value = false
    novoAgendamento()
}

const buscarAgendamento = async () => {
    erro.value = null
    try {
        const res = await fetch(`${API_URL}/agendamento/${codigoBusca.value}`)
        if (!res.ok) throw new Error("Agendamento não encontrado.")
        agendamentoEncontrado.value = await res.json()
        step.value = 11 // Tela de visualização
    } catch (e) {
        erro.value = "Código inválido ou agendamento não encontrado."
    }
}

const iniciarRemarcacao = async () => {
    modoRemarcacao.value = true
    // Define o serviço selecionado como o do agendamento original para pular o Step 1
    servicoSelecionado.value = agendamentoEncontrado.value.servico
    
    // Precisamos definir o profissionalIdConfirmado para buscar slots corretamente
    profissionalIdConfirmado.value = agendamentoEncontrado.value.profissional.id
    
    step.value = 2 // Vai direto para escolha de data
}

const selecionarSlot = (slot) => {
  horaSelecionada.value = slot
}

const irParaIdentificacao = () => {
  if (modoRemarcacao.value) {
      confirmarAgendamento() // Na remarcação, já confirma direto após escolher data
  } else {
      step.value = 3
  }
}

const confirmarAgendamento = async () => {
  erro.value = null
  try {
    const horario = horaSelecionada.value.length === 5 ? `${horaSelecionada.value}:00` : horaSelecionada.value
    const dataHoraIso = `${dataSelecionada.value}T${horario}`

    if (modoRemarcacao.value) {
        // Lógica de Remarcação (PUT)
        const res = await fetch(`${API_URL}/agendamento/${agendamentoEncontrado.value.codigo}/remarcar`, {
            method: 'POST', // Backend usa POST para /remarcar
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ novaDataHora: dataHoraIso })
        })
        if (!res.ok) throw new Error('Erro ao remarcar.')
        
        // Atualiza dados locais para o recibo
        // Mantemos o cliente original
        cliente.value = { nome: agendamentoEncontrado.value.cliente.nome, telefone: agendamentoEncontrado.value.cliente.telefone }
        
    } else {
        // Lógica de Novo Agendamento (POST)
        const payload = {
          servicoId: servicoSelecionado.value.id,
          dataHora: dataHoraIso,
          nomeCliente: cliente.value.nome,
          telefoneCliente: cliente.value.telefone
        }

        const res = await fetch(`${API_URL}/agendar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })

        if (!res.ok) {
           const errJson = await res.json().catch(() => ({}))
           throw new Error(errJson.message || 'Erro ao agendar')
        }
        
        // Se a resposta retornar o objeto criado, poderiamos pegar o codigo aqui
        // Mas o backend retorna o Agendamento, então podemos pegar.
        const agendamentoCriado = await res.json()
        // Usamos agendamentoEncontrado como ref para exibir o código no recibo, mesmo sendo novo
        agendamentoEncontrado.value = agendamentoCriado
        
        // ATUALIZAÇÃO IMPORTANTE (Pedido do Usuário):
        // Garantir que o nome exibido no comprovante seja o que realmente foi salvo no banco.
        // Se o cliente já existia, o backend ignorou o nome digitado e manteve o antigo.
        // Aqui pegamos o nome oficial do objeto retornado.
        cliente.value.nome = agendamentoCriado.cliente.nome; 
    }

    step.value = 4
  } catch (e) {
    erro.value = e.message
  }
}

const formatarDataBonita = (dataStr) => { // Renamed from formatarData
    if(!dataStr) return '';
    const d = new Date(dataStr + 'T12:00:00'); // Força meio dia para evitar fuso
    return d.toLocaleDateString('pt-BR', { weekday: 'short', day: 'numeric', month: 'short' });
}

function imprimirComprovante() {
    window.print();
}

const novoAgendamento = () => { // Renamed from reiniciar
  step.value = 1
  servicoSelecionado.value = null
  dataSelecionada.value = ''
  horaSelecionada.value = null // Updated to horaSelecionada
  cliente.value = { nome: '', telefone: '' }
  erro.value = null
}
</script>

<style scoped>
/* Tema Claro - Estilo Dashboard */
.bg-booking {
  min-height: 100vh;
  background-color: #F8FAFC; 
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: 'Inter', sans-serif;
  position: relative;
}

.overlay-gradient {
  display: none; /* Remove overlay antigo */
}

.booking-card {
  background: #fff;
  padding: 2.5rem;
  border-radius: 24px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08); /* Sombra suave */
  border: 1px solid #f1f5f9;
  position: relative;
  z-index: 10;
}

.title-brand {
    font-size: 1.875rem; 
    font-weight: 700;
    color: #0F172A;
    text-align: center;
    letter-spacing: -0.025em;
}

.text-pink { color: #DB2777; }

.subtitle {
    text-align: center;
    color: #64748B;
    font-size: 0.875rem;
    margin-top: 0.5rem;
    margin-bottom: 2rem;
}

h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #334155;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #f1f5f9;
  padding-bottom: 0.5rem;
}

/* Inputs Modernos */
.label-input {
    display: block;
    font-size: 0.75rem;
    font-weight: 700;
    color: #94A3B8;
    text-transform: uppercase;
    margin-bottom: 0.5rem;
    margin-left: 0.25rem;
}

.input-modern {
    width: 100%;
    padding: 1rem;
    background-color: #F8FAFC;
    border: none;
    border-radius: 1rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: #0F172A;
    transition: all 0.2s;
    outline: none;
}

.input-modern:focus {
    box-shadow: 0 0 0 2px #DB2777;
}

.hint {
    color: #94A3B8;
    font-size: 0.75rem;
    margin-left: 0.5rem;
}

/* Services List */
.service-item {
  background: #F8FAFC;
  padding: 1rem;
  margin-bottom: 0.8rem;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.service-item:hover {
  background: #fff;
  border-color: #DB2777;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(219, 39, 119, 0.1);
}

.service-name { font-weight: 500; color: #334155; }
.service-price { font-weight: 700; color: #DB2777; }

/* Slot Buttons */
.slots-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 2rem;
}

.slot-btn {
  padding: 0.6rem;
  background: #F1F5F9;
  border: none;
  border-radius: 8px;
  color: #475569;
  font-weight: 500;
  cursor: pointer;
  transition: 0.2s;
}

.slot-btn:hover { background: #E2E8F0; }

.slot-btn.selected {
  background: #DB2777;
  color: #fff;
  box-shadow: 0 4px 10px rgba(219, 39, 119, 0.3);
}

/* Actions */
.actions {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 2rem;
}

button {
  width: 100%;
  padding: 1rem;
  border-radius: 1rem;
  border: none;
  cursor: pointer;
  font-weight: 700;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: all 0.2s;
}

.btn-primary {
  background: #DB2777;
  color: #fff;
  box-shadow: 0 4px 15px rgba(219, 39, 119, 0.25);
}

.btn-primary:active { transform: scale(0.98); }
.btn-primary:disabled { background: #CBD5E1; cursor: not-allowed; box-shadow: none; }

.btn-secondary {
  background: transparent;
  color: #64748B;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover { background: #F8FAFC; color: #334155; }

.success { text-align: center; }
.success-icon { font-size: 4rem; margin-bottom: 1rem; display: block; }
.error-msg { background: #FEF2F2; color: #EF4444; padding: 1rem; border-radius: 12px; margin-top: 1rem; text-align: center; font-weight: 500; font-size: 0.875rem; }
.no-slots, .no-data { text-align: center; color: #94A3B8; padding: 1rem; font-style: italic; }

@media print {
  .no-print { display: none !important; }
  .booking-card { box-shadow: none; border: none; padding: 0; max-width: 100%; }
  body { background: white; }
  .bg-booking { padding: 0; align-items: flex-start; }
}
</style>

