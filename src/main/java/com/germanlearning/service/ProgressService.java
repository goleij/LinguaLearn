package com.germanlearning.service;

import com.germanlearning.model.Lesson;
import com.germanlearning.model.Progress;
import com.germanlearning.model.User;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ScoreService scoreService;

    public ProgressService(ProgressRepository progressRepository,
            UserRepository userRepository,
            LessonRepository lessonRepository,
            ScoreService scoreService) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.scoreService = scoreService;
    }

    public Progress getOrCreateProgress(Long userId, Long lessonId) {
        Optional<Progress> existingProgress = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existingProgress.isPresent()) {
            return existingProgress.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        Progress progress = new Progress(user, lesson);
        return progressRepository.save(progress);
    }

    public Progress recordAnswer(Long userId, Long lessonId, boolean isCorrect) {
        Progress progress = getOrCreateProgress(userId, lessonId);

        progress.incrementTotalAnswers();
        if (isCorrect) {
            progress.incrementCorrectAnswers();

            // Only award XP if lesson not already completed (first pass)
            if (!progress.isXpLocked()) {
                int xp = scoreService.calculateXp(true, progress.getCorrectAnswers());
                progress.addXp(xp);

                User user = progress.getUser();
                user.addXp(xp);
                userRepository.save(user);
            }
        }

        progress.setLastAttemptAt(LocalDateTime.now());

        return progressRepository.save(progress);
    }

    public Progress completeLesson(Long userId, Long lessonId) {
        Progress progress = getOrCreateProgress(userId, lessonId);

        if (progress.hasPassedThreshold()) {
            progress.setCompleted(true);
            progress.setXpLocked(true); // Lock XP after passing
            progress.updateBestScore(); // Track best score
            progress.incrementAttempts();
        }

        return progressRepository.save(progress);
    }

    public boolean isLessonCompleted(Long userId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        return progressOpt.map(Progress::isCompleted).orElse(false);
    }

    public double getLessonScore(Long userId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        return progressOpt.map(Progress::getScorePercentage).orElse(0.0);
    }

    public List<Progress> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    public List<Progress> getCompletedLessons(Long userId) {
        return progressRepository.findByUserIdAndCompleted(userId, true);
    }

    public int getTotalXpEarned(Long userId) {
        Integer xp = progressRepository.getTotalXpByUserId(userId);
        return xp != null ? xp : 0;
    }

    public void resetLessonProgressForRetry(Long userId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (progressOpt.isPresent()) {
            Progress progress = progressOpt.get();
            // Only reset current session counters, NOT the completed flag
            progress.setCorrectAnswers(0);
            progress.setTotalAnswers(0);
            // DO NOT reset completed flag - preserve previous completion
            progressRepository.save(progress);
        }
    }
}
