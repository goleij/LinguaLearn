import { expect, test } from '@playwright/test';

/**
 * Every page behind ProtectedRoute has to send an anonymous visitor to the
 * login page. The dashboard lives at "/" in this app rather than at
 * "/dashboard"; both pages below are the ones the router protects.
 */
test.describe('Protected routes', () => {
  for (const path of ['/', '/profile', '/lesson/1']) {
    test(`${path} redirects to the login page without a session`, async ({ page }) => {
      await page.goto(path);

      await expect(page).toHaveURL(/\/login$/);
      await expect(page.getByRole('heading', { name: 'German Learning' })).toBeVisible();
    });
  }
});
