package com.germanlearning.dto;

import com.germanlearning.model.Progress;
import java.time.LocalDateTime;

/**
 * One "Recent Activity" row on the profile page. Unrelated to
 * {@link LessonActivityDto}, which is a step inside a lesson.
 */
public record RecentActivityDto(Long lessonId, String lessonName, LocalDateTime completedAt, int xpEarned) {

    public static RecentActivityDto from(Progress progress) {
        return new RecentActivityDto(
                progress.getLesson().getId(),
                progress.getLesson().getName(),
                progress.getCompletedAt(),
                progress.getXpEarned());
    }
}
