/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          green: '#58CC02',
          blue: '#1CB0F6',
          yellow: '#FFC800',
          orange: '#FF9600',
        },
        ink: {
          DEFAULT: '#3C3C3C',
          muted: '#777777',
          faint: '#AFAFAF',
        },
        surface: {
          page: '#F7F7F7',
          soft: '#F0F0F0',
          grey: '#E5E5E5',
        },
        feedback: {
          correct: '#D7FFB8',
          wrong: '#FFDFE0',
          error: '#FF4B4B',
        },
      },
      boxShadow: {
        card: '0 4px 20px rgba(0,0,0,0.1)',
        unit: '0 2px 10px rgba(0,0,0,0.05)',
        navbar: '0 2px 8px rgba(0,0,0,0.05)',
      },
      backgroundImage: {
        'auth-gradient': 'linear-gradient(135deg, #58CC02 0%, #1CB0F6 100%)',
      },
    },
  },
  plugins: [],
};
