<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const nome = computed(() => authStore.user?.name || 'Visitante');

function sair() {
  authStore.logout();
  router.push('/login');
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-nunito flex flex-col">
    
    <header class="bg-white shadow-sm p-4 flex justify-between items-center">
      <h1 class="text-xl font-bold text-pink-600">Space Nails 💅</h1>
      <div class="flex items-center gap-4">
        <span class="text-gray-600">Olá, {{ nome }}</span>
        <button @click="sair" class="text-sm text-red-500 font-bold border border-red-200 px-3 py-1 rounded hover:bg-red-50">
          Sair
        </button>
      </div>
    </header>

    <main class="flex-1 p-6 max-w-5xl mx-auto w-full">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 class="text-lg font-bold text-gray-800 mb-2">Meus Agendamentos</h2>
          <p class="text-gray-500 text-sm">Você não tem agendamentos futuros.</p>
          <button class="mt-4 w-full bg-pink-500 text-white py-2 rounded-lg font-bold hover:bg-pink-600 transition">
            Novo Agendamento
          </button>
        </div>

        <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 class="text-lg font-bold text-gray-800 mb-2">Serviços</h2>
          <ul class="space-y-2 text-sm text-gray-600">
            <li>• Manicure</li>
            <li>• Pedicure</li>
            <li>• Nail Art</li>
          </ul>
        </div>

        <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 class="text-lg font-bold text-gray-800 mb-2">Perfil</h2>
          <p class="text-gray-500 text-sm mb-4">Gerencie seus dados e preferências.</p>
          <button class="text-pink-500 font-bold text-sm hover:underline">Editar Perfil</button>
        </div>

      </div>
    </main>
  </div>
</template>