import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The Spring Boot backend runs on 8081 (see src/main/resources/application.properties).
// Everything under /api is proxied so the browser stays on one origin and the
// JSESSIONID / XSRF-TOKEN cookies keep working during development.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: false,
      },
    },
  },
});
