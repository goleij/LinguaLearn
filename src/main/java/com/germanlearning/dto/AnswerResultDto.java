package com.germanlearning.dto;

import com.germanlearning.service.ProgressService.AnswerResult;

/** The persisted outcome of one answer, straight from ProgressService. */
public record AnswerResultDto(
        boolean correct,
        String correctAnswer,
        /** Why it was right or wrong, specific enough to act on. */
        String feedback,
        String explanation,
        String hint,
        String phase,
        String skill,
        String cefrLevel,
        String topic,
        int xpAwarded,
        int lessonXpEarned,
        int userTotalXp,
        int currentStreak,
        int correctAnswers,
        int totalAnswers,
        int checkpointCorrectAnswers,
        int checkpointTotalAnswers) {

    public static AnswerResultDto from(AnswerResult result) {
        return new AnswerResultDto(
                result.isCorrect(),
                result.getCorrectAnswer(),
                result.getFeedback(),
                result.getExplanation(),
                result.getHint(),
                result.getPhase() == null ? null : result.getPhase().name(),
                result.getSkill() == null ? null : result.getSkill().name(),
                result.getCefrLevel() == null ? null : result.getCefrLevel().name(),
                result.getTopic(),
                result.getXpAwarded(),
                result.getLessonXpEarned(),
                result.getUserTotalXp(),
                result.getCurrentStreak(),
                result.getCorrectAnswers(),
                result.getTotalAnswers(),
                result.getCheckpointCorrectAnswers(),
                result.getCheckpointTotalAnswers());
    }
}
