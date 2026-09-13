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
  await page.getByRole('button', { name: 'Create Account' }).click();

  // Wait for the request to land before returning. Without this a caller that
  // logs in next can race the POST and be told its own account does not exist.
  // Either outcome ends the wait: the redirect on success, or the toast that
  // explains the refusal, which some specs are specifically looking for.
  await expect(
    page.getByRole('alert').or(page.getByRole('button', { name: 'Log in' })).first(),
  ).toBeVisible();
}

export async function login(page: Page, user: TestUser, password = user.password): Promise<void> {
  await page.goto('/login');
  await waitForAppReady(page);
  await page.getByLabel('Username').fill(user.username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Log in' }).click();
}

export async function loginAndLandOnDashboard(page: Page, user: TestUser): Promise<void> {
  await login(page, user);
  await expect(page.getByRole('heading', { name: `Welcome back, ${user.username}!` })).toBeVisible();
}

/**
 * Reads the XP badge in the navbar, which is where the learner sees it.
 *
 * Both badges are an icon and a bare number, so their accessible label is the
 * only thing that says which number this is — and the only stable handle a
 * test has now that the emoji are gone.
 */
export async function navbarXp(page: Page): Promise<number> {
  return readBadge(page, 'Total XP', 'XP');
}

/** Reads the flame badge, which counts days in a row, not correct answers. */
export async function navbarStreak(page: Page): Promise<number> {
  return readBadge(page, 'Day streak', 'streak');
}

async function readBadge(page: Page, labelPrefix: string, name: string): Promise<number> {
  const label = await page
    .locator(`header [aria-label^="${labelPrefix}"]`)
    .getAttribute('aria-label');

  const match = label?.match(/(\d+)/);
  if (!match) {
    throw new Error(`No ${name} badge in the navbar. Its label read: ${label}`);
  }
  return Number(match[1]);
}
