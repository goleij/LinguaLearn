package com.germanlearning.dto;

import com.germanlearning.model.User;
import java.time.LocalDateTime;

/** The current user as shown in the navbar and on the profile page. */
public record UserDto(
        Long id,
        String username,
        String email,
        int totalXp,
        int currentStreak,
        int longestStreak,
        LocalDateTime createdAt) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getTotalXp(),
                user.getCurrentStreak(),
                user.getLongestStreak(),
                user.getCreatedAt());
    }
}
