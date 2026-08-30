import { expect, test, type Page } from '@playwright/test';
import { loginAndLandOnDashboard, navbarStreak, navbarXp, newUser, register } from './helpers';

/**
 * The Greetings lesson of the seeded German A1 course, played end to end.
 *
 * The answers below are the ones authored in A1BasicsContent, so this suite is
 * deliberately tied to that content: the point is to prove that a real answer
 * travels to the backend, is graded there, is paid for once, and comes back as
 * a number the navbar shows.
 */
const CHOICE_ANSWERS: Record<string, string> = {
  "What does 'Hallo' mean?": 'Hello',
  "What does 'Tschüss' mean?": 'Goodbye',
  'What do you say?': 'Guten Morgen',
  'What did you hear?': 'Good morning',
  "What does 'Guten Tag' mean?": 'Good day',
};

const TEXT_ANSWERS: Record<string, string> = {
  'Complete: Good morning in German': 'Guten Morgen',
  "How do you say 'Good night' in German?": 'Gute Nacht',
};

const PAIRS: Record<string, string> = {
  Hallo: 'Hello',
  Danke: 'Thank you',
  'Tschüss': 'Goodbye',
};

/** Which of the known prompts is currently on screen. */
async function currentPrompt(page: Page, prompts: string[]): Promise<string> {
  for (const prompt of prompts) {
    if (await page.getByText(prompt, { exact: true }).count()) {
      return prompt;
    }
  }
  throw new Error(
    `Unrecognised activity. The card reads:\n${await page.getByRole('main').innerText()}`,
  );
}

/**
 * Plays one activity. Teaching cards are read and dismissed; graded ones are
 * answered correctly, and the answer is checked against the feedback the
 * backend sent so a stale answer key fails loudly instead of silently
 * measuring the wrong thing.
 */
async function playOneActivity(page: Page): Promise<void> {
  const teaching = page.getByRole('button', { name: /^(Got it|Start checkpoint)$/ });
  if (await teaching.count()) {
    await teaching.first().click();
    return;
  }

  if (await page.getByRole('heading', { name: 'Match the pairs' }).count()) {
    for (const [german, english] of Object.entries(PAIRS)) {
      await page.getByRole('button', { name: german, exact: true }).click();
      await page.getByRole('button', { name: english, exact: true }).click();
    }
  } else if (await page.getByRole('heading', { name: 'Fill in the blank' }).count()) {
    const prompt = await currentPrompt(page, Object.keys(TEXT_ANSWERS));
    await page.getByPlaceholder('...').fill(TEXT_ANSWERS[prompt]);
  } else {
    const prompt = await currentPrompt(page, Object.keys(CHOICE_ANSWERS));
    await page.getByRole('radio', { name: CHOICE_ANSWERS[prompt], exact: true }).check();
  }

  await page.getByRole('button', { name: 'Check Answer' }).click();

  await expect(page.getByRole('heading', { name: 'Correct!' })).toBeVisible();
  await page.getByRole('button', { name: 'Continue' }).click();
}

/** Runs the lesson to its result screen. */
async function playWholeLesson(page: Page): Promise<void> {
  const result = page.getByRole('heading', { name: 'Lesson Complete!' });
  // Shown between the last answer and the result the backend is still computing
  const finishing = page.getByText('Finishing lesson');

  // The seeded lesson has twelve activities; the cap only stops a runaway loop
  for (let guard = 0; guard < 40; guard++) {
    if ((await result.count()) || (await finishing.count())) {
      await expect(result).toBeVisible();
      return;
    }
    await playOneActivity(page);
  }

  throw new Error('The lesson never reached its result screen');
}

async function openGreetings(page: Page): Promise<void> {
  await page.getByRole('button', { name: /Greetings/ }).click();
  await expect(page.getByRole('heading', { name: 'Greetings' })).toBeVisible();
}

// The second test needs the lesson the first one completed, so they share an
// account and run in order.
test.describe.serial('Lesson and XP', () => {
  const user = newUser();

  test('answering correctly earns XP and completes the lesson', async ({ page }) => {
    await register(page, user);
    await loginAndLandOnDashboard(page, user);

    const xpBefore = await navbarXp(page);
    expect(xpBefore).toBe(0);
    // Nothing has been started yet, so no day has been counted
    expect(await navbarStreak(page)).toBe(0);

    await openGreetings(page);

    // Opening the lesson counts today, and the flame stays on days from then
    // on: answering does not move it, and a new attempt does not clear it
    expect(await navbarStreak(page)).toBe(1);

    await playWholeLesson(page);

    expect(await navbarStreak(page)).toBe(1);
    await expect(page.getByText(/\+\d+ XP earned!/)).toBeVisible();
    await expect(page.getByText("Great job! You've unlocked the next lesson!")).toBeVisible();

    // The badge shows what the backend stored, not a number the page invented
    const xpAfter = await navbarXp(page);
    expect(xpAfter).toBeGreaterThan(xpBefore);

    await page.getByRole('button', { name: 'Back to Dashboard' }).click();
    await expect(page.getByRole('banner')).toContainText(`XP: ${xpAfter}`);
  });

  test('the XP survives a reload and the lesson counts as done', async ({ page }) => {
    await loginAndLandOnDashboard(page, user);

    expect(await navbarXp(page)).toBeGreaterThan(0);
    // A completed lesson unlocks the next one, which starts out locked
    await expect(page.getByRole('button', { name: /Introducing Yourself/ })).toBeVisible();
  });

  test('the profile reports the day streak rather than a permanent zero', async ({ page }) => {
    await loginAndLandOnDashboard(page, user);

    await page.getByRole('link', { name: 'Profile' }).click();

    await expect(page.getByText('1 day', { exact: true })).toBeVisible();
    await expect(page.getByText('1 lessons')).toBeVisible();
  });

  test('replaying a completed lesson pays no XP a second time', async ({ page }) => {
    await loginAndLandOnDashboard(page, user);

    const xpBefore = await navbarXp(page);
    await openGreetings(page);
    await playWholeLesson(page);

    await expect(page.getByText('Practice Mode - No XP Earned')).toBeVisible();
    expect(await navbarXp(page)).toBe(xpBefore);
  });
});
