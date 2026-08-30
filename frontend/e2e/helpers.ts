import { expect, type Page } from '@playwright/test';

export interface TestUser {
  username: string;
  email: string;
  password: string;
}

/**
 * The e2e database is kept between runs so the seeded courses stay in place,
 * which means every spec has to bring an account nobody has used before.
 */
export function newUser(): TestUser {
  const suffix = `${Date.now().toString(36)}${Math.floor(Math.random() * 10_000)}`;
  return {
    username: `e2e${suffix}`,
    email: `e2e${suffix}@example.com`,
    password: 'secret123',
  };
}

/**
 * Waits until the app has finished booting.
 *
 * AuthProvider calls /auth/me on mount, and the CSRF cookie the fetch wrapper
 * has to echo back arrives with that response. Playwright can fill a form and
 * press the button before it lands, and the POST is then rejected with 403 --
 * a race no human typing a username and two passwords could win, but one every
 * spec would hit. The cookie is the signal that the app is ready.
 */
async function waitForAppReady(page: Page): Promise<void> {
  await expect
    .poll(async () => (await page.context().cookies()).some((c) => c.name === 'XSRF-TOKEN'), {
      message: 'the CSRF cookie never arrived, so the app never finished loading',
    })
    .toBe(true);
}

export async function register(page: Page, user: TestUser, email = user.email): Promise<void> {
  await page.goto('/register');
  await waitForAppReady(page);
  await page.getByLabel('Username').fill(user.username);
  await page.getByLabel('Email').fill(email);
  // "Password" on its own would also match "Confirm Password"
  await page.getByLabel('Password', { exact: true }).fill(user.password);
  await page.getByLabel('Confirm Password').fill(user.password);
  await submitAndWaitFor(page, '/api/auth/register', 'Create Account');
}

export async function login(page: Page, user: TestUser, password = user.password): Promise<void> {
  await page.goto('/login');
  await waitForAppReady(page);
  await page.getByLabel('Username').fill(user.username);
  await page.getByLabel('Password').fill(password);
  await submitAndWaitFor(page, '/api/auth/login', 'Log in');
}

/**
 * Presses a submit button and waits for the request behind it to come back.
 *
 * Without this the helper returns while the POST is still in flight, and the
 * next navigation cancels it -- which looked exactly like a rejected account,
 * because the following login then found no such user.
 */
async function submitAndWaitFor(page: Page, apiPath: string, button: string): Promise<void> {
  const response = page.waitForResponse((it) => it.url().includes(apiPath));
  await page.getByRole('button', { name: button }).click();
  await response;
}

export async function loginAndLandOnDashboard(page: Page, user: TestUser): Promise<void> {
  await login(page, user);
  await expect(page.getByRole('heading', { name: `Welcome back, ${user.username}!` })).toBeVisible();
}

/** Reads the XP badge in the navbar, which is where the learner sees it. */
export async function navbarXp(page: Page): Promise<number> {
  return readBadge(page, /XP:\s*(\d+)/, 'XP');
}

/** Reads the flame badge, which counts days in a row, not correct answers. */
export async function navbarStreak(page: Page): Promise<number> {
  return readBadge(page, /\u{1F525}\s*(\d+)/u, 'streak');
}

async function readBadge(page: Page, pattern: RegExp, name: string): Promise<number> {
  const header = await page.getByRole('banner').innerText();
  const match = header.match(pattern);
  if (!match) {
    throw new Error(`No ${name} badge in the navbar. It reads: ${header}`);
  }
  return Number(match[1]);
}
