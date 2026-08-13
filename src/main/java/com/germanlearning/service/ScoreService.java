package com.germanlearning.service;

import org.springframework.stereotype.Service;

/**
 * Single source of truth for the XP formula.
 *
 * No other class is allowed to compute XP: {@link ProgressService} asks this
 * service for the amount and persists exactly that value.
 */
@Service
public class ScoreService {

    private static final int BASE_XP = 10;
    private static final int STREAK_BONUS_THRESHOLD = 3;
    private static final int STREAK_BONUS_XP = 5;

    /**
     * XP for one correct answer.
     *
     * @param exerciseXpReward the XP configured on the exercise (falls back to
     *                         {@link #BASE_XP} when the exercise has no value)
     * @param currentStreak    number of consecutive correct answers in this
     *                         lesson attempt, including the answer being scored
     */
    public int calculateXpForCorrectAnswer(int exerciseXpReward, int currentStreak) {
        int xp = exerciseXpReward > 0 ? exerciseXpReward : BASE_XP;

        if (currentStreak >= STREAK_BONUS_THRESHOLD) {
            xp += STREAK_BONUS_XP;
        }
        if (currentStreak >= 5) {
            xp += 2;
        }
        if (currentStreak >= 10) {
            xp += 3;
        }

        return xp;
    }

    public int getBaseXp() {
        return BASE_XP;
    }

    public int getStreakBonusThreshold() {
        return STREAK_BONUS_THRESHOLD;
    }
}
