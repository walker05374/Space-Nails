import { defineStore } from 'pinia';
import { ref, watch } from 'vue';

export const useAccessibilityStore = defineStore('accessibility', () => {
  // --- TAMANHO DA FONTE ---
  const fontSize = ref(Number(localStorage.getItem('a11y_fontSize')) || 100);

  // --- ALTO CONTRASTE ---
  // Recupera do localStorage (true/false)
  const highContrast = ref(localStorage.getItem('a11y_highContrast') === 'true');

  // Ações Fonte
  function increaseFont() {
    if (fontSize.value < 150) fontSize.value += 10; 
  }

  function decreaseFont() {
    if (fontSize.value > 70) fontSize.value -= 10; 
  }

  // Ações Contraste
  function toggleContrast() {
    highContrast.value = !highContrast.value;
  }

  function reset() {
    fontSize.value = 100;
    highContrast.value = false;
  }

  // Observador Fonte
  watch(fontSize, (newVal) => {
    document.documentElement.style.fontSize = `${newVal}%`;
    localStorage.setItem('a11y_fontSize', newVal);
  }, { immediate: true });

  // Observador Contraste
  watch(highContrast, (newVal) => {
    if (newVal) {
      document.documentElement.classList.add('high-contrast');
    } else {
      document.documentElement.classList.remove('high-contrast');
    }
    localStorage.setItem('a11y_highContrast', newVal);
  }, { immediate: true });

  return { 
    fontSize, 
    highContrast,
    increaseFont, 
    decreaseFont, 
    toggleContrast,
    reset
  };
});