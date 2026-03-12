package com.germanlearning.service;

import org.springframework.stereotype.Service;

@Service
public class ScoreService {

    private static final int BASE_XP = 10;
    private static final int STREAK_BONUS_THRESHOLD = 3;
    private static final int STREAK_BONUS_XP = 5;

    public int calculateXp(boolean isCorrect, int currentStreak) {
        if (!isCorrect) {
            return 0;
        }

        int xp = BASE_XP;
        
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
