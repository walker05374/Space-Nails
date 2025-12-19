<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import PinModal from '@/components/PinModal.vue';
import AvatarSelectorModal from '@/components/AvatarSelectorModal.vue';

const router = useRouter();
const authStore = useAuthStore();
const dependentes = ref([]);
const loading = ref(true);

const showPinModal = ref(false);
const showAddChildModal = ref(false);
const pinAction = ref(''); 
const showAvatarSelector = ref(false);
const modoGerenciamento = ref(false); 
const alunoParaExcluir = ref(null);

const cadastrando = ref(false);
const novoAluno = ref({ nome: '', idade: '', genero: 'M', avatar: '' });

onMounted(async () => {
  await carregarDependentes();
});

async function carregarDependentes() {
  try {
    const response = await api.get('/api/responsavel/dependentes');
    dependentes.value = response.data;
  } catch (e) { 
    console.error(e); 
  } finally { 
    loading.value = false; 
  }
}

function selecionarAluno(aluno) {
  if (modoGerenciamento.value) return; 
  authStore.selecionarCrianca(aluno);
  router.push('/home');
}

function iniciarExclusao(aluno) {
    alunoParaExcluir.value = aluno;
    pinAction.value = 'DELETE_CHILD';
    showPinModal.value = true;
}

function pedirPin(acao) {
  pinAction.value = acao;
  showPinModal.value = true;
}

async function onPinSuccess() {
  showPinModal.value = false;
  
  if (pinAction.value === 'CONFIG') {
    router.push('/responsavel'); 
  } 
  else if (pinAction.value === 'ADD_CHILD') {
    novoAluno.value = { nome: '', idade: '', genero: 'M', avatar: `https://api.dicebear.com/7.x/adventurer/svg?seed=${Date.now()}` };
    showAddChildModal.value = true;
  }
  else if (pinAction.value === 'DELETE_CHILD' && alunoParaExcluir.value) {
      try {
          await api.delete(`/api/responsavel/dependentes/${alunoParaExcluir.value.id}`);
          await carregarDependentes();
      } catch(e) { alert("Erro ao excluir."); }
      alunoParaExcluir.value = null;
  }
}

async function salvarNovoAluno() {
  if (!novoAluno.value.nome) return alert("Preencha o nome!");
  cadastrando.value = true;
  try {
    const ano = new Date().getFullYear() - novoAluno.value.idade;
    await api.post('/api/responsavel/dependentes', {
      nome: novoAluno.value.nome,
      dataNascimento: `${ano}-01-01`,
      avatarUrl: novoAluno.value.avatar
    });
    showAddChildModal.value = false;
    await carregarDependentes();
  } catch(e) { alert("Erro ao criar perfil."); } finally { cadastrando.value = false; }
}

function handleAvatarSelect(url) { 
    novoAluno.value.avatar = url; 
    showAvatarSelector.value = false; 
}
</script>

<template>
  <div class="min-h-screen bg-brand-bg flex flex-col items-center justify-center p-4 font-sans relative">
    
    <button @click="authStore.logout()" class="absolute top-6 right-6 text-xs font-bold text-red-500 bg-white px-4 py-2 rounded-xl shadow-sm border border-gray-100 hover:bg-red-50 transition-colors z-20">
      SAIR
    </button>

    <div class="text-center mb-10 z-10">
      <h1 class="text-2xl md:text-3xl font-bold text-brand-dark">Quem está acessando?</h1>
      <div class="mt-2 flex items-center justify-center gap-2">
         <span class="text-sm text-gray-500">Gerenciar Perfis</span>
         <button @click="modoGerenciamento = !modoGerenciamento" :class="['w-8 h-8 rounded-full flex items-center justify-center transition-all', modoGerenciamento ? 'bg-brand-primary text-white' : 'bg-gray-200 text-gray-500']">
            ⚙️
         </button>
      </div>
    </div>

    <div v-if="!loading" class="flex flex-wrap justify-center gap-8 z-10 w-full max-w-4xl">
      
      <div @click="pedirPin('CONFIG')" class="flex flex-col items-center group cursor-pointer">
        <div class="w-28 h-28 rounded-full border-4 border-white shadow-card group-hover:scale-105 group-hover:border-brand-primary transition-all overflow-hidden bg-gray-100">
           <img :src="authStore.user?.avatarUrl" class="w-full h-full object-cover">
        </div>
        <span class="mt-4 font-bold text-gray-600 group-hover:text-brand-primary">
            {{ authStore.user?.name || 'Admin' }}
        </span>
      </div>

      <div v-for="aluno in dependentes" :key="aluno.id" @click="selecionarAluno(aluno)" class="flex flex-col items-center group cursor-pointer relative">
        <button v-if="modoGerenciamento" @click.stop="iniciarExclusao(aluno)" class="absolute -top-1 -right-1 z-20 bg-red-500 text-white w-6 h-6 rounded-full flex items-center justify-center shadow-lg hover:scale-110">✕</button>

        <div class="w-28 h-28 rounded-full border-4 border-white shadow-card group-hover:scale-105 group-hover:border-green-400 transition-all overflow-hidden relative bg-gray-100">
           <div v-if="modoGerenciamento" class="absolute inset-0 bg-white/60 z-10"></div> 
           <img :src="aluno.avatarUrl" class="w-full h-full object-cover">
        </div>
        <span class="mt-4 font-bold text-gray-600 group-hover:text-green-500">
            {{ aluno.nome }}
        </span>
      </div>

      <div @click="pedirPin('ADD_CHILD')" class="flex flex-col items-center group cursor-pointer opacity-60 hover:opacity-100 transition-all">
        <div class="w-28 h-28 rounded-full border-4 border-dashed border-gray-300 flex items-center justify-center bg-transparent group-hover:border-brand-primary transition-all">
           <span class="text-4xl text-gray-300 group-hover:text-brand-primary font-light">+</span>
        </div>
        <span class="mt-4 font-bold text-gray-400 group-hover:text-brand-primary">Novo Perfil</span>
      </div>
    </div>

    <PinModal v-if="showPinModal" @close="showPinModal = false" @success="onPinSuccess" />
    
    <div v-if="showAddChildModal" class="fixed inset-0 bg-brand-dark/20 backdrop-blur-sm z-50 flex items-center justify-center p-4">
       <div class="bg-white p-8 rounded-3xl w-full max-w-sm shadow-2xl">
        <h2 class="text-xl font-bold text-brand-dark mb-6 text-center">Criar Perfil</h2>
        <form @submit.prevent="salvarNovoAluno" class="space-y-4">
          
          <div class="flex justify-center mb-4">
            <div @click="showAvatarSelector = true" class="w-20 h-20 rounded-full bg-gray-50 border border-gray-200 flex items-center justify-center cursor-pointer overflow-hidden hover:border-brand-primary">
              <img v-if="novoAluno.avatar" :src="novoAluno.avatar" class="w-full h-full object-cover">
              <span v-else class="text-xs text-gray-400 font-bold">Foto</span>
            </div>
          </div>

          <input v-model="novoAluno.nome" required placeholder="Nome" class="w-full px-4 py-3 bg-gray-50 rounded-xl text-sm font-bold outline-none focus:ring-2 focus:ring-brand-primary/50">
          
          <div class="grid grid-cols-2 gap-3">
            <input type="number" v-model="novoAluno.idade" required placeholder="Idade" class="w-full px-4 py-3 bg-gray-50 rounded-xl text-sm font-bold outline-none">
            <select v-model="novoAluno.genero" class="w-full px-4 py-3 bg-gray-50 rounded-xl text-sm font-bold outline-none">
                <option value="M">M</option>
                <option value="F">F</option>
            </select>
          </div>

          <div class="flex gap-3 pt-2">
            <button type="button" @click="showAddChildModal = false" class="flex-1 py-3 bg-gray-100 text-gray-500 rounded-xl text-xs font-bold">Cancelar</button>
            <button type="submit" :disabled="cadastrando" class="flex-1 py-3 bg-brand-primary text-white rounded-xl text-xs font-bold">Salvar</button>
          </div>
        </form>
      </div>
    </div>

    <AvatarSelectorModal v-if="showAvatarSelector" @close="showAvatarSelector = false" @select="handleAvatarSelect" />
  </div>
</template>