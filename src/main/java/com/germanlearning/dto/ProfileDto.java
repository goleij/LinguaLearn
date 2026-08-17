package com.germanlearning.dto;

import java.util.List;

/** Everything the profile page shows. */
public record ProfileDto(
        UserDto user,
        int completedLessonsCount,
        List<RecentActivityDto> recentActivity) {
}
