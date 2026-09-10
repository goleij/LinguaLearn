package com.germanlearning.dto;

import com.germanlearning.model.SavedWord;
import java.time.LocalDateTime;

/** One card in the word bank, with enough state to draw its progress. */
public record SavedWordDto(
        Long id,
        String german,
        String english,
        String source,
        String sourceLabel,
        String topic,
        String cefrLevel,
        String lessonName,
        int box,
        int maxBox,
        boolean due,
        boolean learned,
        int reviewCount,
        int correctCount,
        LocalDateTime dueAt,
        LocalDateTime addedAt) {

    public static SavedWordDto from(SavedWord word) {
        return new SavedWordDto(
                word.getId(),
                word.getGerman(),
                word.getEnglish(),
                word.getSource().name(),
                word.getSource().getLabel(),
                word.getTopic(),
                word.getCefrLevel() == null ? null : word.getCefrLevel().name(),
                word.getLessonName(),
                word.getBox(),
                SavedWord.LAST_BOX,
                word.isDue(),
                word.isLearned(),
                word.getReviewCount(),
                word.getCorrectCount(),
                word.getDueAt(),
                word.getAddedAt());
    }
}
