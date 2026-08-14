package com.germanlearning.dto;

import com.germanlearning.model.Progress;
import java.time.LocalDateTime;

/** One "Recent Activity" row on the profile page. */
public record ActivityDto(Long lessonId, String lessonName, LocalDateTime completedAt, int xpEarned) {

    public static ActivityDto from(Progress progress) {
        return new ActivityDto(
                progress.getLesson().getId(),
                progress.getLesson().getName(),
                progress.getCompletedAt(),
                progress.getXpEarned());
    }
}
