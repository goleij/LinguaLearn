package com.germanlearning.dto;

import com.germanlearning.service.ProgressService.LessonCompletionResult;

/** The result screen data, straight from ProgressService. */
public record LessonCompletionDto(
        boolean passed,
        boolean practiceMode,
        boolean completed,
        boolean hasCheckpoint,
        double scorePercentage,
        int correctAnswers,
        int totalAnswers,
        int checkpointCorrectAnswers,
        int checkpointTotalAnswers,
        int lessonXpEarned,
        int userTotalXp,
        double passThreshold) {

    public static LessonCompletionDto from(LessonCompletionResult result, double passThreshold) {
        return new LessonCompletionDto(
                result.isPassed(),
                result.isPracticeMode(),
                result.isCompleted(),
                result.isHasCheckpoint(),
                result.getScorePercentage(),
                result.getCorrectAnswers(),
                result.getTotalAnswers(),
                result.getCheckpointCorrectAnswers(),
                result.getCheckpointTotalAnswers(),
                result.getLessonXpEarned(),
                result.getUserTotalXp(),
                passThreshold);
    }
}
