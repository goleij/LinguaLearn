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

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateXp(Long userId, int xpAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.addXp(xpAmount);
        return userRepository.save(user);
    }

    public User updateStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveAt() != null
                ? user.getLastActiveAt().toLocalDate()
                : null;

        if (lastActive == null) {
            user.setCurrentStreak(1);
        } else if (lastActive.equals(today.minusDays(1))) {
            user.incrementStreak();
        } else if (!lastActive.equals(today)) {
            user.resetStreak();
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
