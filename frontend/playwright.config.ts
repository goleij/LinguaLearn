import { defineConfig, devices } from '@playwright/test';

/**
 * Browser tests against the real stack.
 *
 * Both servers are started by Playwright itself and neither one touches the
 * database you learn from: the backend runs under the "e2e" profile, which
 * points Spring at target/lingualearn-e2e.db, and the dev server is told to
 * proxy /api there instead of to the usual 8081.
 */
const BACKEND_PORT = 8082;
const FRONTEND_PORT = 5175;

export default defineConfig({
  testDir: './e2e',
  // One backend, one database, one server-side session store: running the specs
  // side by side would have them fighting over all three.
  fullyParallel: false,
  workers: 1,
  timeout: 90_000,
  expect: { timeout: 15_000 },
  retries: process.env.CI ? 1 : 0,
  forbidOnly: !!process.env.CI,
  reporter: process.env.CI
    ? [['github'], ['html', { open: 'never' }]]
    : [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: `http://localhost:${FRONTEND_PORT}`,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command: 'mvn -q spring-boot:run -Dspring-boot.run.profiles=e2e',
      cwd: '..',
      port: BACKEND_PORT,
      reuseExistingServer: !process.env.CI,
      // A cold Maven start downloads nothing but still has to compile and seed
      timeout: 300_000,
      stdout: 'pipe',
      stderr: 'pipe',
    },
    {
      command: `npm run dev -- --port ${FRONTEND_PORT} --strictPort`,
      env: { BACKEND_URL: `http://localhost:${BACKEND_PORT}` },
      port: FRONTEND_PORT,
      reuseExistingServer: !process.env.CI,
      timeout: 120_000,
    },
  ],
});
