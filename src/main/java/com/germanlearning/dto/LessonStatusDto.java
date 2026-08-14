package com.germanlearning.dto;

import com.germanlearning.service.LessonService.LessonStatus;

/** A lesson tile on the dashboard: locked, unlocked or completed. */
public record LessonStatusDto(
        Long id,
        String name,
        String description,
        int orderIndex,
        boolean unlocked,
        boolean completed,
        double progressPercentage) {

    public static LessonStatusDto from(LessonStatus status) {
        return new LessonStatusDto(
                status.getLesson().getId(),
                status.getLesson().getName(),
                status.getLesson().getDescription(),
                status.getLesson().getOrderIndex(),
                status.isUnlocked(),
                status.isCompleted(),
                status.getProgressPercentage());
    }
}
