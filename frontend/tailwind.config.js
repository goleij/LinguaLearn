/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        // Outfit for headings, Inter for text. The fallbacks are the stack the
        // app used before, so nothing shifts badly if Google Fonts is blocked.
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'],
        display: ['Outfit', 'Inter', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      colors: {
        brand: {
          green: '#58CC02',
          // The pressed/hover green, which was repeated as a raw hex in six
          // components before it had a name
          'green-dark': '#46A302',
          // The brand green as *text* on a light surface (#58CC02 measures
          // 2.09:1). Fills keep the original.
          'green-ink': '#108400',
          blue: '#1CB0F6',
          // The brand blue and orange are bright enough to fail as small text
          // (2.44:1 and 2.20:1 on white). These are the same hues darkened
          // until they pass on the grey page surface as well as on white, for
          // labels and links on light backgrounds. The originals stay exactly
          // as they are for fills, badges and icons.
          'blue-ink': '#0076BC',
          yellow: '#FFC800',
          orange: '#FF9600',
          'orange-ink': '#A84900',
        },
        ink: {
          DEFAULT: '#3C3C3C',
          // Nudged from #777777, which measured 4.48:1 on white and missed the
          // 4.5 body-text threshold. This value clears it on the grey page
          // surface too, which is where most body copy actually sits.
          muted: '#707070',
          // Nudged from #AFAFAF (2.19:1). Reserved for genuinely secondary
          // text; anything a reader must not miss uses ink-muted.
          faint: '#8C8C8C',
        },
        surface: {
          page: '#F7F7F7',
          soft: '#F0F0F0',
          grey: '#E5E5E5',
          // The landing page's dark panels, where the bright brand colours
          // read as accents instead of as the whole surface
          dark: '#0B1220',
          darker: '#060B14',
        },
        feedback: {
          correct: '#D7FFB8',
          wrong: '#FFDFE0',
          error: '#FF4B4B',
          // The error red as text on the wrong-answer panel, where the bright
          // one measures 2.65:1
          'error-ink': '#CC1818',
        },
      },
      // The radii the app actually uses, so components stop inventing them
      borderRadius: {
        card: '20px',
        panel: '24px',
        pill: '25px',
      },
      maxWidth: {
        container: '1120px',
        prose: '460px',
      },
      boxShadow: {
        card: '0 4px 20px rgba(0,0,0,0.1)',
        unit: '0 2px 10px rgba(0,0,0,0.05)',
        navbar: '0 2px 8px rgba(0,0,0,0.05)',
        // Layered rather than one blur, so the floating panels on the landing
        // page read as objects with weight
        float: '0 1px 2px rgba(8,15,26,0.16), 0 12px 24px -8px rgba(8,15,26,0.28), 0 40px 64px -32px rgba(8,15,26,0.4)',
        lift: '0 1px 2px rgba(8,15,26,0.1), 0 8px 20px -6px rgba(8,15,26,0.14)',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        rise: {
          from: { opacity: '0', transform: 'translateY(12px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        float: 'float 6s ease-in-out infinite',
        rise: 'rise 0.6s cubic-bezier(0.22, 1, 0.36, 1) both',
      },
    },
  },
  plugins: [],
};
