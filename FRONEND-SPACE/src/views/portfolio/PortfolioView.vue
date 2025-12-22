<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const fotos = ref([]);
const servicos = ref([]);
const carregando = ref(true);
const erro = ref(null);

const novaFoto = ref({
  titulo: '',
  imagemUrl: '',
  file: null, // Novo campo para arquivo
  servicoId: null
});

const arquivoSelecionado = ref(null); // Ref para o input file

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
    if(!confirm("Tem certeza?")) return;
    try {
        await api.delete(`/api/portfolio/${id}`);
        fotos.value = fotos.value.filter(f => f.id !== id);
    } catch(e) {
        alert("Erro ao excluir.");
    }
}
</script>

<template>
    <div class="space-y-8 animate-fade-in">
        <!-- Header Section -->
        <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-center gap-4">
           <div>
               <h2 class="text-xl font-bold text-[#0F172A] flex items-center gap-2">
                   <span>📸</span> Conheça meu Trabalho (Portfólio)
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
            <span class="text-4xl block mb-2">🖼️</span>
            <p class="text-gray-500 font-medium">Sua galeria está vazia.</p>
            <p class="text-xs text-gray-400">Comece postando sua primeira foto acima!</p>
        </div>
        
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <div v-for="foto in fotos" :key="foto.id" class="group relative bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg transition-all">
                <div class="aspect-square bg-gray-100 relative overflow-hidden">
                    <img :src="foto.imagemUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="Portfolio">
                    <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                        <button class="bg-white/20 hover:bg-white/40 text-white p-2 rounded-full backdrop-blur-sm transition-colors" title="Ver Fullscreen">👁️</button>
                    </div>
                </div>
                <div class="p-4">
                    <h4 class="font-bold text-[#0F172A] truncate">{{ foto.titulo }}</h4>
                    <div class="flex justify-between items-center mt-2">
                         <span class="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded-lg">
                             {{ servicos.find(s => s.id === foto.servico?.id)?.nome || 'Geral' }}
                         </span>
                         <span class="text-xs font-bold text-gray-400 flex items-center gap-1">
                             👁️ {{ foto.clicks || 0 }}
                         </span>
                    </div>
                    <button @click="excluirFoto(foto.id)" class="mt-3 w-full py-2 text-xs font-bold text-red-500 border border-red-100 rounded-lg hover:bg-red-50 transition-colors">
                        Excluir
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.label-input { @apply block text-xs font-bold text-gray-400 uppercase mb-1 ml-1; }
.input-modern { @apply w-full px-4 py-3 rounded-xl bg-gray-50 border-none outline-none focus:ring-2 focus:ring-[#DB2777]/50 text-gray-800 text-sm transition-all; }
</style>
