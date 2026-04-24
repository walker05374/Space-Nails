/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Paleta Modern Chic
        'brand-bg': '#F8FAFC',       // Cinza muito claro, quase branco (fundo geral)
        'brand-surface': '#FFFFFF',   // Branco puro (cards)
        'brand-dark': '#0F172A',      // Azul marinho quase preto (textos principais)
        'brand-gray': '#64748B',      // Cinza moderno (textos secundários)
        'brand-primary': '#DB2777',   // Pink Raspberry (Ação/Destaque) - Vibrante
        'brand-primary-light': '#FCE7F3', // Fundo suave do Pink
        'brand-accent': '#8B5CF6',    // Roxo moderno (detalhes sutis)
        'status-success': '#10B981',  // Verde Esmeralda
        'status-error': '#EF4444',    // Vermelho Vivo
      },
      fontFamily: {
        'sans': ['"Poppins"', 'sans-serif'], // Fonte geométrica moderna
      },
      boxShadow: {
        'card': '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)', // Sombra sutil
        'float': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)', // Sombra forte para botões
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
        '3xl': '2rem', // Arredondamento moderno
      }
    },
  },
  plugins: [],
}