import { expect, test } from '@playwright/test';

/**
 * Every page behind ProtectedRoute has to send an anonymous visitor to the
 * login page. "/" is deliberately not one of them: it is the public landing
 * page, and only steps aside once somebody is signed in.
 */
test.describe('Protected routes', () => {
  for (const path of ['/learn', '/profile', '/words', '/lesson/1']) {
    test(`${path} redirects to the login page without a session`, async ({ page }) => {
      await page.goto(path);

      await expect(page).toHaveURL(/\/login$/);
      // The sign-in form itself, rather than the wording around it
      await expect(page.getByRole('button', { name: 'Log in' })).toBeVisible();
    });
  }

  test('the landing page is public and offers a way in', async ({ page }) => {
    await page.goto('/');

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByRole('heading', { name: /German that actually stays/ })).toBeVisible();
    // Both the header and the hero offer a way in; the header one is enough
    await expect(page.getByRole('banner').getByRole('link', { name: 'Log in' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Start learning free' })).toBeVisible();
  });
});
