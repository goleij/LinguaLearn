package com.germanlearning.dto;

import java.util.List;

/**
 * A lesson ready to be played. When the lesson is locked the exercises are
 * withheld and the UI shows the "This lesson is locked!" notification.
 */
public record LessonDetailDto(
        Long id,
        String name,
        String description,
        boolean unlocked,
        boolean practiceMode,
        List<ExerciseDto> exercises) {
}
