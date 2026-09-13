/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The Spring Boot backend runs on 8081 (see src/main/resources/application.properties).
// Everything under /api is proxied so the browser stays on one origin and the
// JSESSIONID / XSRF-TOKEN cookies keep working during development.
//
// BACKEND_URL overrides the target: the Playwright suite starts its own backend
// on 8082 with its own database, and points the dev server at that one.
const backendUrl = process.env.BACKEND_URL ?? 'http://localhost:8081';

// NOTE: editing tailwind.config.js does NOT take effect in a running dev
// server. PostCSS resolves that file once and caches it for the life of the
// Node process, and Vite's own restart reuses the process, so the new
// utilities are simply missing from the served CSS — which shows up as a
// broken-looking page rather than as an error. Stop the server and start it
// again after changing design tokens.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: backendUrl,
        changeOrigin: false,
      },
    },
  },
  test: {
    // Component tests need a DOM; the Playwright specs under e2e/ drive a real
    // browser instead and must not be picked up by Vitest.
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    exclude: ['e2e/**', 'node_modules/**', 'dist/**'],
    css: false,
    restoreMocks: true,
  },
});
