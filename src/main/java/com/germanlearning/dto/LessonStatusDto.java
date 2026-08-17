package com.germanlearning.dto;

import com.germanlearning.model.Lesson;
import com.germanlearning.service.LessonService.LessonStatus;

/** A lesson tile on the dashboard: locked, unlocked or completed. */
public record LessonStatusDto(
        Long id,
        String name,
        String description,
        String cefrLevel,
        int orderIndex,
        boolean unlocked,
        boolean completed,
        double progressPercentage) {

    public static LessonStatusDto from(LessonStatus status) {
        Lesson lesson = status.getLesson();
        return new LessonStatusDto(
                lesson.getId(),
                lesson.getName(),
                lesson.getDescription(),
                lesson.resolveCefrLevel() == null ? null : lesson.resolveCefrLevel().name(),
                lesson.getOrderIndex(),
                status.isUnlocked(),
                status.isCompleted(),
                status.getProgressPercentage());
    }
}
