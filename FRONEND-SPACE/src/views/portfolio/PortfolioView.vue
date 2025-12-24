<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const fotos = ref([]);
const servicos = ref([]);
const carregando = ref(true);
const erro = ref(null);
const fotoSelecionada = ref(null);

const novaFoto = ref({
  titulo: '',
  imagemUrl: '',
  file: null, // Novo campo para arquivo
  servicoId: null,
});

const arquivoSelecionado = ref(null); // Ref para o input file

function abrirFoto(foto) {
    fotoSelecionada.value = foto;
}

function fecharFoto() {
    fotoSelecionada.value = null;
}

// Estados de Edição e Feedback
const modalEdicaoOpen = ref(false);
const itemEmEdicao = ref(null);
const modalFeedbackOpen = ref(false);
const msgFeedback = ref('');
const tipoFeedback = ref('sucesso'); // sucesso, erro

// EDITAR
function abrirModalEdicao(foto) {
    if (!foto) return;
    itemEmEdicao.value = { ...foto, servicoId: foto.servico?.id || null };
    modalEdicaoOpen.value = true;
}

async function salvarEdicao() {
    if (!itemEmEdicao.value) return;
    try {
        const payload = {
            titulo: itemEmEdicao.value.titulo,
            servicoId: itemEmEdicao.value.servicoId
        };
        const res = await api.put(`/api/portfolio/${itemEmEdicao.value.id}`, payload);
        
        // Atualiza na lista local
        const idx = fotos.value.findIndex(f => f.id === res.data.id);
        if (idx !== -1) {
            fotos.value[idx] = res.data;
        }
        
        closeModals();
        mostrarFeedback("Item atualizado com sucesso!");
    } catch(e) {
        mostrarFeedback("Erro ao atualizar item.", 'erro');
    }
}

// FEEDBACK
function mostrarFeedback(msg, tipo = 'sucesso') {
    msgFeedback.value = msg;
    tipoFeedback.value = tipo;
    modalFeedbackOpen.value = true;
    setTimeout(() => { modalFeedbackOpen.value = false; }, 3000);
}

function closeModals() {
    modalEdicaoOpen.value = false;
    itemEmEdicao.value = null;
}

onMounted(() => {
    carregarFotos();
    carregarServicos();
});

async function carregarFotos() {
    carregando.value = true;
    try {
        const res = await api.get('/api/portfolio/meus');
        fotos.value = res.data;
    } catch(e) {
        console.error(e);
        erro.value = "Erro ao carregar galeria.";
    } finally {
        carregando.value = false;
    }
}


// Preveiw da Imagem
const previewImagem = ref(null);

async function carregarServicos() {
    try {
        // Força busca atualizada
        const res = await api.get('/api/servicos'); // Endpoint correto
        servicos.value = res.data;
    } catch(e) { console.error(e); }
}

// Limpa preview quando destrói ou reseta
function limparForm() {
    novaFoto.value = { titulo: '', imagemUrl: '', file: null, servicoId: null };
    previewImagem.value = null;
    if(arquivoSelecionado.value) arquivoSelecionado.value.value = '';
}

function onFileChange(e) {
    const file = e.target.files[0];
    if (file) {
        novaFoto.value.file = file;
        // Cria preview
        previewImagem.value = URL.createObjectURL(file);
    }
}

async function publicarFoto() {
    if(!novaFoto.value.titulo) return alert("Preencha o título.");
    if(!novaFoto.value.file && !novaFoto.value.imagemUrl) return alert("Envie um arquivo ou uma URL.");
    
    try {
        const formData = new FormData();
        formData.append('titulo', novaFoto.value.titulo);
        if (novaFoto.value.imagemUrl) formData.append('imagemUrl', novaFoto.value.imagemUrl);
        if (novaFoto.value.servicoId) formData.append('servicoId', novaFoto.value.servicoId);
        if (novaFoto.value.file) formData.append('file', novaFoto.value.file);

        await api.post('/api/portfolio', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });

        alert("Foto publicada!");
        limparForm();
        carregarFotos();
    } catch(e) {
        alert("Erro ao publicar: " + (e.response?.data?.message || e.message));
    }
}

async function excluirFoto(id) {
    if(!confirm("Tem certeza que deseja excluir esta foto permanentemente?")) return;
    try {
        await api.delete(`/api/portfolio/${id}`);
        fotos.value = fotos.value.filter(f => f.id !== id);
        mostrarFeedback("Foto excluída com sucesso!", 'sucesso');
    } catch(e) {
        mostrarFeedback("Erro ao excluir foto.", 'erro');
    }
}
</script>

<template>
    <div class="space-y-8 animate-fade-in">
        <!-- Header Section -->
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-center gap-4">
           <div>
               <h2 class="text-xl font-bold text-[#0F172A] flex items-center gap-2">
                   <span>
                       <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                   </span> Conheça meu Trabalho (Portfólio)
               </h2>
               <p class="text-gray-500 text-sm mt-1 max-w-xl">
                   Adicione fotos dos seus trabalhos para que os clientes vejam ao agendar. 
                   Elas aparecerão na sua página pública e nos detalhes do serviço.
               </p>
           </div>
           
           <div class="flex gap-4">
              <div class="bg-pink-50 p-3 rounded-2xl text-center min-w-[100px]">
                  <p class="text-xs font-bold text-gray-400 uppercase">Fotos</p>
                  <p class="text-2xl font-bold text-[#DB2777]">{{ fotos.length }}</p>
              </div>
              <div class="bg-blue-50 p-3 rounded-2xl text-center min-w-[100px]">
                  <p class="text-xs font-bold text-gray-400 uppercase">Visualizações</p>
                  <p class="text-2xl font-bold text-blue-600">{{ fotos.reduce((acc, f) => acc + (f.clicks || 0), 0) }}</p>
              </div>
           </div>
        </div>

        <!-- Form Section -->
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
            <h3 class="font-bold text-[#0F172A] mb-4">Nova Publicação</h3>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                <div class="md:col-span-1">
                    <label class="label-input">Título / Descrição</label>
                    <input v-model="novaFoto.titulo" class="input-modern" placeholder="Ex: Unhas Decoradas Natal">
                </div>
                <div class="md:col-span-2">
                    <label class="label-input">Imagem (Arquivo ou Link)</label>
                    <div class="flex flex-col gap-2">
                        <input type="file" ref="arquivoSelecionado" @change="onFileChange" accept="image/*" class="text-xs text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-xs file:font-semibold file:bg-pink-50 file:text-pink-700 hover:file:bg-pink-100"/>
                        <div class="flex items-center gap-2">
                             <span class="text-xs font-bold text-gray-300">OU</span>
                             <input v-model="novaFoto.imagemUrl" class="input-modern bg-white" placeholder="Cole uma URL https://...">
                        </div>
                    </div>
                </div>
                
                <!-- PREVIEW DA IMAGEM -->
                <div v-if="previewImagem || novaFoto.imagemUrl" class="md:col-span-full mt-2">
                    <p class="text-xs font-bold text-gray-400 uppercase mb-2">Pré-visualização</p>
                    <div class="w-full h-40 bg-gray-50 rounded-xl overflow-hidden border border-gray-200">
                        <img :src="previewImagem || novaFoto.imagemUrl" class="w-full h-full object-contain">
                    </div>
                </div>

                <div class="md:col-span-1">
                    <label class="label-input">Serviço Relacionado</label>
                    <select v-model="novaFoto.servicoId" class="input-modern">
                        <option :value="null">Geral (Sem serviço específico)</option>
                        <option v-for="s in servicos" :key="s.id" :value="s.id">{{ s.nome }}</option>
                    </select>
                </div>
            </div>
            <button @click="publicarFoto" class="mt-4 w-full bg-[#DB2777] text-white font-bold py-3 rounded-xl hover:brightness-110 transition-colors uppercase text-xs tracking-wide">
                + Publicar Foto
            </button>
        </div>

        <!-- Gallery Grid -->
        <div v-if="carregando" class="text-center py-10 text-gray-400">Carregando fotos...</div>
        <div v-else-if="fotos.length === 0" class="text-center py-16 bg-gray-50 rounded-3xl border border-dashed border-gray-200">
            <span class="text-4xl block mb-2 text-gray-300">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
            </span>
            <p class="text-gray-500 font-medium">Sua galeria está vazia.</p>
            <p class="text-xs text-gray-400">Comece postando sua primeira foto acima!</p>
        </div>
        
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <div v-for="foto in fotos" :key="foto.id" class="group relative bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg transition-all">
                <div class="aspect-square bg-gray-100 relative overflow-hidden cursor-pointer" @click="abrirFoto(foto)">
                    <img :src="foto.imagemUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="Portfolio">
                    <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                        <button class="bg-white/20 hover:bg-white/40 text-white p-2 rounded-full backdrop-blur-sm transition-colors" title="Ver Fullscreen">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                        </button>
                    </div>
                </div>
                <div class="p-4">
                    <h4 class="font-bold text-[#0F172A] truncate">{{ foto.titulo }}</h4>
                    <div class="flex justify-between items-center mt-2">
                         <span class="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded-lg">
                             {{ servicos.find(s => s.id === foto.servico?.id)?.nome || 'Geral' }}
                         </span>
                         <span class="text-xs font-bold text-gray-400 flex items-center gap-1">
                             <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg> {{ foto.clicks || 0 }}
                         </span>
                    </div>
                    <div class="flex gap-2 mt-3">
                         <button @click.stop="abrirModalEdicao(foto)" class="flex-1 py-2 text-xs font-bold text-blue-500 border border-blue-100 rounded-lg hover:bg-blue-50 transition-colors">
                            Editar
                        </button>
                        <button @click.stop="excluirFoto(foto.id)" class="flex-1 py-2 text-xs font-bold text-red-500 border border-red-100 rounded-lg hover:bg-red-50 transition-colors">
                            Excluir
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- LIGHTBOX / MODAL DE VISUALIZAÇÃO -->
        <div v-if="fotoSelecionada" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/95 backdrop-blur-md animate-fade-in" @click.self="fecharFoto">
            <button @click="fecharFoto" class="absolute top-4 right-4 text-white/50 hover:text-white p-2 transition-colors z-50">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </button>

            <div class="max-w-4xl w-full max-h-[90vh] flex flex-col items-center">
                <img :src="fotoSelecionada.imagemUrl" class="max-w-full max-h-[80vh] object-contain rounded-lg shadow-2xl mb-4">
                
                <div class="text-center text-white">
                    <h2 class="text-xl font-bold">{{ fotoSelecionada.titulo }}</h2>
                    <p class="text-pink-400 font-medium text-sm mt-1">
                        {{ servicos.find(s => s.id === fotoSelecionada.servico?.id)?.nome || 'Geral' }}
                    </p>
                    <p class="text-[10px] text-gray-400 mt-2">Visualizações: {{ fotoSelecionada.clicks || 0 }}</p>
                </div>
            </div>
        </div>
        <!-- MODAL EDIÇÃO -->
        <div v-if="modalEdicaoOpen" class="fixed inset-0 bg-[#0F172A]/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div class="bg-white w-full max-w-sm rounded-3xl p-6 shadow-2xl">
                <h3 class="font-bold text-[#0F172A] mb-4">Editar Detalhes</h3>
                <div class="space-y-3">
                    <input v-model="itemEmEdicao.titulo" placeholder="Título" class="input-modern">
                    <select v-model="itemEmEdicao.servicoId" class="input-modern">
                        <option :value="null">Geral (Sem serviço)</option>
                        <option v-for="s in servicos" :key="s.id" :value="s.id">{{ s.nome }}</option>
                    </select>
                    <div class="flex gap-2 pt-2">
                        <button @click="closeModals" class="btn-secondary">Cancelar</button>
                        <button @click="salvarEdicao" class="flex-1 py-3 bg-[#DB2777] text-white rounded-xl text-xs font-bold hover:brightness-110 uppercase">Salvar</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- MODAL FEEDBACK (SUCCESS/ERROR) -->
        <div v-if="modalFeedbackOpen" class="fixed bottom-4 right-4 z-[60] animate-fade-in">
             <div class="px-6 py-4 rounded-2xl shadow-2xl text-white font-bold text-sm flex items-center gap-3"
                  :class="tipoFeedback === 'sucesso' ? 'bg-green-500' : 'bg-red-500'">
                 <span v-if="tipoFeedback === 'sucesso'">
                     <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                 </span>
                 <span v-else>
                     <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                 </span>
                 {{ msgFeedback }}
             </div>
        </div>
    </div>
</template>

<style scoped>
.label-input { @apply block text-xs font-bold text-gray-400 uppercase mb-1 ml-1; }
.input-modern { @apply w-full px-4 py-3 rounded-xl bg-gray-50 border-none outline-none focus:ring-2 focus:ring-[#DB2777]/50 text-gray-800 text-sm transition-all; }
</style>
