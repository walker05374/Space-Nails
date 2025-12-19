<script setup>
import { RouterView } from 'vue-router'
import { useAccessibilityStore } from '@/stores/accessibility';
import { onMounted, ref } from 'vue';

const a11y = useAccessibilityStore();
const toolbarOpen = ref(false);

onMounted(() => {
  document.documentElement.style.fontSize = `${a11y.fontSize}%`;
  // Script VLibras mantido
  if (!document.getElementById('vlibras-script')) {
    const script = document.createElement('script');
    script.id = 'vlibras-script';
    script.src = 'https://vlibras.gov.br/app/vlibras-plugin.js';
    script.async = true;
    script.onload = () => {
      new window.VLibras.Widget('https://vlibras.gov.br/app');
    };
    document.body.appendChild(script);
  }
});
</script>

<template>
  <div vw class="enabled">
    <div vw-access-button class="active"></div>
    <div vw-plugin-wrapper>
      <div class="vw-plugin-top-wrapper"></div>
    </div>
  </div>

  <div class="fixed top-24 right-4 z-[9999] flex flex-col items-end gap-3">
    <button 
      @click="toolbarOpen = !toolbarOpen"
      class="w-12 h-12 bg-white text-brand-dark shadow-float rounded-full flex items-center justify-center hover:scale-110 transition-transform duration-300 border border-gray-100"
      title="Acessibilidade"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
    </button>

    <div v-if="toolbarOpen" class="bg-white/90 backdrop-blur-md border border-gray-200 shadow-float rounded-2xl p-4 flex flex-col gap-3 w-40 animate-slide-in">
      <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest text-center">Tamanho Fonte</span>
      <div class="flex items-center justify-between bg-gray-100 rounded-xl p-1">
        <button @click="a11y.decreaseFont" class="w-8 h-8 flex items-center justify-center font-bold text-gray-600 hover:bg-white rounded-lg transition-colors">A-</button>
        <span class="text-xs font-bold text-brand-primary">{{ a11y.fontSize }}%</span>
        <button @click="a11y.increaseFont" class="w-8 h-8 flex items-center justify-center font-bold text-gray-600 hover:bg-white rounded-lg transition-colors">A+</button>
      </div>
    </div>
  </div>

  <RouterView />
</template>

<style scoped>
.animate-slide-in { animation: slideIn 0.2s cubic-bezier(0.2, 0.8, 0.2, 1); }
@keyframes slideIn { from { opacity: 0; transform: translateX(10px); } to { opacity: 1; transform: translateX(0); } }
</style>