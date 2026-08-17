package com.germanlearning.controller;

import com.germanlearning.dto.RecentActivityDto;
import com.germanlearning.dto.ProfileDto;
import com.germanlearning.dto.UserDto;
import com.germanlearning.model.User;
import com.germanlearning.service.CurrentUserService;
import com.germanlearning.service.ProgressService;
import com.germanlearning.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final ProgressService progressService;

    public ProfileController(CurrentUserService currentUserService,
            UserService userService,
            ProgressService progressService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
        this.progressService = progressService;
    }

    @GetMapping
    public ProfileDto getProfile() {
        User user = currentUserService.requireCurrentUser();

        // Same as ProfileView: the five most recent completed lessons
        List<RecentActivityDto> recentActivity = progressService.getCompletedLessons(user.getId()).stream()
                .limit(5)
                .map(RecentActivityDto::from)
                .toList();

        return new ProfileDto(
                UserDto.from(user),
                userService.getCompletedLessonsCount(user.getId()),
                recentActivity);
    }
}
