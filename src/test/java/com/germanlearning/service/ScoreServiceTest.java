package com.germanlearning.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreServiceTest {

    private final ScoreService scoreService = new ScoreService();

    @Test
    void usesTheExerciseRewardAsBase() {
        assertEquals(20, scoreService.calculateXpForCorrectAnswer(20, 1));
    }

    @Test
    void fallsBackToBaseXpWhenExerciseHasNoReward() {
        assertEquals(scoreService.getBaseXp(), scoreService.calculateXpForCorrectAnswer(0, 1));
    }

    @Test
    void addsStreakBonuses() {
        assertEquals(10, scoreService.calculateXpForCorrectAnswer(10, 2));
        assertEquals(15, scoreService.calculateXpForCorrectAnswer(10, 3));
        assertEquals(17, scoreService.calculateXpForCorrectAnswer(10, 5));
        assertEquals(20, scoreService.calculateXpForCorrectAnswer(10, 10));
    }
}
