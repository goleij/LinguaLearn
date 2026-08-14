package com.germanlearning.dto;

import com.germanlearning.service.ProgressService.AnswerResult;

/** The persisted outcome of one answer, straight from ProgressService. */
public record AnswerResultDto(
        boolean correct,
        String correctAnswer,
        String explanation,
        int xpAwarded,
        int lessonXpEarned,
        int userTotalXp,
        int currentStreak,
        int correctAnswers,
        int totalAnswers) {

    public static AnswerResultDto from(AnswerResult result) {
        return new AnswerResultDto(
                result.isCorrect(),
                result.getCorrectAnswer(),
                result.getExplanation(),
                result.getXpAwarded(),
                result.getLessonXpEarned(),
                result.getUserTotalXp(),
                result.getCurrentStreak(),
                result.getCorrectAnswers(),
                result.getTotalAnswers());
    }
}
