import { expect, test } from '@playwright/test';
import { login, loginAndLandOnDashboard, newUser, register } from './helpers';

/**
 * The account lifecycle through the browser: create one, get in, get out.
 * These exercise the parts no unit test can reach — the session cookie, the
 * CSRF token and the redirects between the two auth pages.
 */
test.describe('Authentication', () => {
  test('registering a new account leads to the login page', async ({ page }) => {
    const user = newUser();

    await register(page, user);

    await expect(page).toHaveURL(/\/login$/);
    // first(): the dev server runs under React StrictMode, which invokes the
    // effect behind this toast twice and so renders it twice
    await expect(page.getByRole('alert').first()).toContainText('Account created successfully');
  });

  test('registering a username that is taken shows the reason', async ({ page }) => {
    const user = newUser();
    await register(page, user);
    await expect(page).toHaveURL(/\/login$/);

    // Same username, a free email address, so only the username can be at fault
    await register(page, user, `other-${user.email}`);

    await expect(page.getByRole('alert').first()).toContainText('Username already exists');
    await expect(page).toHaveURL(/\/register$/);
  });

  test('logging in opens the dashboard', async ({ page }) => {
    const user = newUser();
    await register(page, user);

    await loginAndLandOnDashboard(page, user);

    await expect(page).toHaveURL(/\/learn$/);
    // The badges are icon plus number now, so the label is what identifies them
    await expect(page.locator('header [aria-label^="Total XP"]')).toBeVisible();
  });

  test('the wrong password is refused', async ({ page }) => {
    const user = newUser();
    await register(page, user);

    await login(page, user, 'definitely-not-the-password');

    await expect(page.getByText('Incorrect username or password')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('logging out returns to the login page and ends the session', async ({ page }) => {
    const user = newUser();
    await register(page, user);
    await loginAndLandOnDashboard(page, user);

    await page.getByRole('button', { name: 'Logout' }).click();
    await expect(page).toHaveURL(/\/login$/);

    // The session is really gone, not just the page
    await page.goto('/learn');
    await expect(page).toHaveURL(/\/login$/);
  });
});
