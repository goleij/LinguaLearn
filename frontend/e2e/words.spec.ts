import { expect, test, type Page } from '@playwright/test';
import { loginAndLandOnDashboard, newUser, register } from './helpers';

/**
 * The word bank, from picking a word up in a lesson to reviewing it.
 *
 * The two ways in are covered separately: the star in a lesson's word list is
 * deliberate, and a wrong answer files a word on its own. The second is the
 * one worth proving end to end, because it depends on the backend resolving
 * the answer back to a pair the lesson actually taught.
 */
test.describe.serial('Word bank', () => {
  const user = newUser();

  test('a word saved from a lesson appears in the word bank', async ({ page }) => {
    await register(page, user);
    await loginAndLandOnDashboard(page, user);

    await openGreetings(page);

    // The vocabulary card is the first activity of this lesson
    await page.getByRole('button', { name: 'Save Hallo to my words' }).click();
    await expect(page.getByRole('button', { name: 'Hallo is saved' })).toBeVisible();

    await goToWordBank(page);

    await expect(page.getByText('Hallo', { exact: true })).toBeVisible();
    await expect(page.getByText('Hello', { exact: true })).toBeVisible();
  });

  test('a word missed in a lesson files itself', async ({ page }) => {
    await loginAndLandOnDashboard(page, user);
    await openGreetings(page);

    // Walk to the fill-in-the-blank and get it deliberately wrong
    await page.getByRole('button', { name: 'Got it' }).click();
    await stepThroughTo(page, 'Fill in the blank');

    await page.getByPlaceholder('...').fill('Guten Tag');
    await page.getByRole('button', { name: 'Check Answer' }).click();
    await expect(page.getByRole('heading', { name: 'Not quite right' })).toBeVisible();

    await goToWordBank(page);

    // The lesson taught "Guten Morgen = Good morning", so that is what is filed
    await expect(page.getByText('Guten Morgen', { exact: true })).toBeVisible();
    await expect(page.getByText('Good morning', { exact: true })).toBeVisible();
    await expect(page.getByText('Missed in a lesson').first()).toBeVisible();
  });

  test('reviewing a card schedules it for later', async ({ page }) => {
    await loginAndLandOnDashboard(page, user);
    await goToWordBank(page);

    const reviewButton = page.getByRole('button', { name: /^Review \d+ words?$/ });
    await expect(reviewButton).toBeVisible();
    await reviewButton.click();

    const showMeaning = page.getByRole('button', { name: 'Show meaning' });
    const finished = page.getByRole('heading', { name: 'Review complete!' });

    // Answer every card until the queue is empty. Waiting for either outcome
    // first stops the loop racing the last card off the screen.
    for (let guard = 0; guard < 20; guard++) {
      await expect(showMeaning.or(finished)).toBeVisible();
      if (await finished.count()) {
        break;
      }
      await showMeaning.click();
      await page.getByRole('button', { name: 'Knew it' }).click();
    }

    await expect(page.getByRole('heading', { name: 'Review complete!' })).toBeVisible();
    await page.getByRole('button', { name: 'Back to my words' }).click();

    // Everything the learner just remembered is scheduled, so nothing is due
    await expect(
      page.getByText('Nothing due right now — everything is scheduled for later.'),
    ).toBeVisible();
  });
});

async function openGreetings(page: Page): Promise<void> {
  await page.getByRole('button', { name: /Greetings/ }).click();
  await expect(page.getByRole('heading', { name: 'Greetings' })).toBeVisible();
}

async function goToWordBank(page: Page): Promise<void> {
  await page.getByRole('link', { name: /My words/ }).click();
  await expect(page.getByRole('heading', { name: 'My words' })).toBeVisible();
}

/** Clicks through teaching cards until the wanted activity is on screen. */
async function stepThroughTo(page: Page, heading: string): Promise<void> {
  for (let guard = 0; guard < 12; guard++) {
    if (await page.getByRole('heading', { name: heading }).count()) {
      return;
    }

    const teaching = page.getByRole('button', { name: /^(Got it|Start checkpoint)$/ });
    if (await teaching.count()) {
      await teaching.first().click();
      continue;
    }

    // A graded activity in the way: answer it however and move on
    const radio = page.getByRole('radio').first();
    if (await radio.count()) {
      await radio.check();
      await page.getByRole('button', { name: 'Check Answer' }).click();
      await page.getByRole('button', { name: 'Continue' }).click();
      continue;
    }

    throw new Error(`Stuck before reaching "${heading}"`);
  }

  throw new Error(`Never reached "${heading}"`);
}
