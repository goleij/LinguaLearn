package com.germanlearning.service;

import com.germanlearning.model.User;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;

    public UserService(UserRepository userRepository, ProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
    }

    /**
     * XP is awarded exclusively by
     * {@link ProgressService#submitAnswer(Long, Long, Long, String)}; this
     * service only reads it.
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * The daily streak: how many days in a row the learner has come back.
     *
     * Called whenever a lesson attempt is started, so the streak follows actual
     * learning rather than how long a session happened to stay open. Coming
     * back twice on the same day does not extend it, and missing a day starts
     * it over at one.
     */
    public User updateStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveAt() != null
                ? user.getLastActiveAt().toLocalDate()
                : null;

        if (today.equals(lastActive)) {
            // Already counted today. A brand new account is created with
            // lastActiveAt set to now, so without this its very first day would
            // fall through here and leave the streak at zero until tomorrow.
            if (user.getCurrentStreak() == 0) {
                user.setCurrentStreak(1);
            }
        } else if (today.minusDays(1).equals(lastActive)) {
            user.incrementStreak();
        } else {
            // Either the first day ever, or a day was missed
            user.setCurrentStreak(1);
        }

        user.setLastActiveAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public int getTotalXp(Long userId) {
        Integer xp = progressRepository.getTotalXpByUserId(userId);
        return xp != null ? xp : 0;
    }

    public int getCompletedLessonsCount(Long userId) {
        return progressRepository.countCompletedLessons(userId);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
