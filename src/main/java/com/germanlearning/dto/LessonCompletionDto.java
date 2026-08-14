package com.germanlearning.dto;

import com.germanlearning.service.ProgressService.LessonCompletionResult;

/** The completion screen data, straight from ProgressService. */
public record LessonCompletionDto(
        boolean passed,
        boolean practiceMode,
        boolean completed,
        double scorePercentage,
        int correctAnswers,
        int totalAnswers,
        int lessonXpEarned,
        int userTotalXp,
        double passThreshold) {

    public static LessonCompletionDto from(LessonCompletionResult result, double passThreshold) {
        return new LessonCompletionDto(
                result.isPassed(),
                result.isPracticeMode(),
                result.isCompleted(),
                result.getScorePercentage(),
                result.getCorrectAnswers(),
                result.getTotalAnswers(),
                result.getLessonXpEarned(),
                result.getUserTotalXp(),
                passThreshold);
    }
}
