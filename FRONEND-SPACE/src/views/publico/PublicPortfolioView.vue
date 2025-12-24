<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';

const route = useRoute();
const fotos = ref([]);
const carregando = ref(true);
const erro = ref(null);
const profissional = ref(null);

// Modal de Visualização Fullscreen
const fotoSelecionada = ref(null);

onMounted(async () => {
    try {
        const inviteCode = route.params.inviteCode;
        if (!inviteCode) throw new Error("Código inválido.");

        // 1. Busca Profissional pelo Invite Code (Vamos usar o endpoint existente de busca pública se houver, ou criar lógica)
        // Como não temos endpoint direto "buscar por invite code" publico além do agendamento, 
        // vamos reutilizar a lógica de buscar profissional.
        // Se o inviteCode for slug (nome), funciona. Se for codigo, precisamos garantir.
        // O Public Booking usa /api/public/profissional/slug/{slug}.
        
        // Usa api service que já tem baseURL configurada
        const resProf = await api.get(`/api/public/profissional/slug/${inviteCode}`);
        
        // Axios já joga erro se status não for 2xx, então não precisa de if(!res.ok)
        profissional.value = resProf.data;

        // 2. Busca Fotos do Profissional
        const resFotos = await api.get(`/api/public/portfolio/${profissional.value.id}`);
        fotos.value = resFotos.data;

    } catch(e) {
        erro.value = "Não foi possível carregar o portfólio.";
        console.error(e);
    } finally {
        carregando.value = false;
    }
});

async function abrirFoto(foto) {
    fotoSelecionada.value = foto;
    
    // Registra visualização (ÚNICA POR SESSÃO)
    // Se já viu essa foto nesta sessão, não conta de novo.
    const chaveStorage = `viewed_foto_${foto.id}`;
    if (!sessionStorage.getItem(chaveStorage)) {
        try {
            await api.post(`/api/public/portfolio/${foto.id}/click`);
            sessionStorage.setItem(chaveStorage, 'true');
        } catch(e) { console.error(e); }
    }
}

function fecharFoto() {
    fotoSelecionada.value = null;
}
</script>

<template>
    <div class="min-h-screen bg-gray-50 font-sans pb-10">
        <!-- Header -->
        <header class="bg-white sticky top-0 z-30 shadow-sm border-b border-gray-100">
            <div class="max-w-5xl mx-auto px-4 py-4 flex justify-between items-center">
                <div class="flex items-center gap-3">
                    <button @click="$router.push(`/agendar/${route.params.inviteCode}`)" class="p-2 -ml-2 text-gray-400 hover:text-[#DB2777] rounded-full hover:bg-pink-50 transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
                    </button>
                    <img src="/icon.png" class="w-10 h-10 animate-bounce-slow" />
                    <div>
                        <h1 class="font-bold text-[#0F172A] text-lg leading-tight">Portfólio</h1>
                        <p v-if="profissional" class="text-xs text-gray-500">de {{ profissional.nome }}</p>
                    </div>
                </div>
                
                <button @click="$router.push(`/agendar/${route.params.inviteCode}`)" class="bg-[#DB2777] text-white text-xs font-bold px-4 py-2 rounded-full hover:brightness-110 transition-shadow shadow-lg shadow-pink-500/30">
                    Agendar Horário
                </button>
            </div>
        </header>

        <main class="max-w-5xl mx-auto px-4 py-6">
            <div v-if="carregando" class="flex flex-col items-center justify-center py-20">
                <div class="w-10 h-10 border-4 border-[#DB2777] border-t-transparent rounded-full animate-spin"></div>
                <p class="mt-4 text-gray-400 text-sm font-medium">Carregando fotos...</p>
            </div>

            <div v-else-if="erro" class="text-center py-20 text-red-500 bg-white rounded-3xl p-8 shadow-sm">
                {{ erro }}
            </div>

            <div v-else-if="fotos.length === 0" class="text-center py-20 bg-white rounded-3xl border border-dashed border-gray-200">
                <span class="text-4xl block mb-2 opacity-50">📷</span>
                <p class="text-gray-500 font-medium">Nenhuma foto publicada ainda.</p>
            </div>

            <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            <div v-for="foto in fotos" :key="foto.id" class="break-inside-avoid mb-4 group relative rounded-2xl overflow-hidden cursor-zoom-in shadow-sm hover:shadow-lg transition-all" @click="abrirFoto(foto)">
                <div class="aspect-square bg-gray-100 relative">
                    <img :src="foto.imagemUrl" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" loading="lazy">
                    <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                         <span class="text-white font-bold text-xs tracking-wider uppercase">Ver Foto</span>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <!-- LIGHTBOX / MODAL -->
    <div v-if="fotoSelecionada" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/95 backdrop-blur-md animate-fade-in" @click.self="fecharFoto">
        <button @click="fecharFoto" class="absolute top-4 right-4 text-white/50 hover:text-white p-2 transition-colors z-50">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        </button>

        <div class="max-w-4xl w-full max-h-[90vh] flex flex-col items-center">
            <img :src="fotoSelecionada.imagemUrl" class="max-w-full max-h-[80vh] object-contain rounded-lg shadow-2xl mb-4">
            
            <div class="text-center text-white">
                <h2 class="text-xl font-bold">{{ fotoSelecionada.titulo }}</h2>
                <p v-if="fotoSelecionada.servico" class="text-pink-400 font-medium text-sm mt-1">{{ fotoSelecionada.servico.nome }}</p>
            </div>
        </div>
    </div>
</div>
</template>

<style scoped>
/* Grid Layout handled by Tailwind classes */
</style>
