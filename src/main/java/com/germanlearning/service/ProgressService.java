package com.germanlearning.service;

import com.germanlearning.model.Exercise;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.Progress;
import com.germanlearning.model.User;
import com.germanlearning.repository.ExerciseRepository;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Owns the whole XP / progress flow.
 *
 * The UI never computes or accumulates XP itself: it calls
 * {@link #submitAnswer(Long, Long, Long, String)} per answer and
 * {@link #completeLesson(Long, Long)} at the end, and renders the values
 * returned by those methods, which are exactly the values written to the
 * database inside the same transaction.
 */
@Service
@Transactional
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseService exerciseService;
    private final ScoreService scoreService;

    public ProgressService(ProgressRepository progressRepository,
            UserRepository userRepository,
            LessonRepository lessonRepository,
            ExerciseRepository exerciseRepository,
            ExerciseService exerciseService,
            ScoreService scoreService) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.exerciseService = exerciseService;
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

    /**
     * Starts a fresh attempt at a lesson: the per-attempt counters are cleared
     * so the score always reflects the current run, while everything earned
     * before (XP, completion, best score, exercises that already paid out) is
     * kept.
     */
    public Progress startLessonAttempt(Long userId, Long lessonId) {
        Progress progress = getOrCreateProgress(userId, lessonId);
        progress.setCorrectAnswers(0);
        progress.setTotalAnswers(0);
        progress.resetStreak();
        progress.setLastAttemptAt(LocalDateTime.now());
        return progressRepository.saveAndFlush(progress);
    }

    /**
     * The single entry point for answering an exercise: validates the answer,
     * updates the streak, awards XP once per exercise and persists everything.
     *
     * @return the persisted outcome, including the XP actually stored
     */
    public AnswerResult submitAnswer(Long userId, Long lessonId, Long exerciseId, String answer) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        if (exercise.getLesson() == null || !exercise.getLesson().getId().equals(lessonId)) {
            throw new IllegalArgumentException("Exercise " + exerciseId + " does not belong to lesson " + lessonId);
        }

        ExerciseService.ExerciseResult validation = exerciseService.validateAnswer(exercise, answer);
        boolean correct = validation.isCorrect();

        Progress progress = getOrCreateProgress(userId, lessonId);
        progress.incrementTotalAnswers();

        if (correct) {
            progress.incrementCorrectAnswers();
            progress.incrementStreak();
        } else {
            progress.resetStreak();
        }

        int xpAwarded = 0;
        if (correct && progress.isXpPayableFor(exerciseId)) {
            xpAwarded = scoreService.calculateXpForCorrectAnswer(
                    exercise.getXpReward(), progress.getCurrentStreak());

            progress.addXp(xpAwarded);
            progress.markXpAwardedFor(exerciseId);

            User user = progress.getUser();
            user.addXp(xpAwarded);
            userRepository.saveAndFlush(user);
        }

        progress.setLastAttemptAt(LocalDateTime.now());
        Progress saved = progressRepository.saveAndFlush(progress);

        return new AnswerResult(
                correct,
                validation.getCorrectAnswer(),
                validation.getExplanation(),
                xpAwarded,
                saved.getXpEarned(),
                saved.getUser().getTotalXp(),
                saved.getCurrentStreak(),
                saved.getCorrectAnswers(),
                saved.getTotalAnswers());
    }

    /**
     * Finishes an attempt. Passing the threshold marks the lesson completed
     * (which unlocks the next one) and locks XP so later practice runs pay
     * nothing.
     */
    public LessonCompletionResult completeLesson(Long userId, Long lessonId) {
        Progress progress = getOrCreateProgress(userId, lessonId);

        boolean practiceMode = progress.isCompleted();
        boolean passed = progress.hasPassedThreshold();

        if (passed) {
            progress.setCompleted(true);
            progress.setXpLocked(true); // Lock XP after passing
            progress.updateBestScore(); // Track best score
            progress.incrementAttempts();
        }

        Progress saved = progressRepository.saveAndFlush(progress);

        return new LessonCompletionResult(
                passed,
                practiceMode,
                saved.isCompleted(),
                saved.getScorePercentage(),
                saved.getCorrectAnswers(),
                saved.getTotalAnswers(),
                saved.getXpEarned(),
                saved.getUser().getTotalXp());
    }

    @Transactional(readOnly = true)
    public boolean isLessonCompleted(Long userId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        return progressOpt.map(Progress::isCompleted).orElse(false);
    }

    @Transactional(readOnly = true)
    public double getLessonScore(Long userId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, lessonId);
        return progressOpt.map(Progress::getScorePercentage).orElse(0.0);
    }

    @Transactional(readOnly = true)
    public List<Progress> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Progress> getCompletedLessons(Long userId) {
        return progressRepository.findByUserIdAndCompleted(userId, true);
    }

    @Transactional(readOnly = true)
    public int getTotalXpEarned(Long userId) {
        Integer xp = progressRepository.getTotalXpByUserId(userId);
        return xp != null ? xp : 0;
    }

    public double getPassThresholdPercentage() {
        return Progress.PASS_THRESHOLD_PERCENTAGE;
    }

    /** Outcome of one answer, as persisted. */
    public static class AnswerResult {
        private final boolean correct;
        private final String correctAnswer;
        private final String explanation;
        private final int xpAwarded;
        private final int lessonXpEarned;
        private final int userTotalXp;
        private final int currentStreak;
        private final int correctAnswers;
        private final int totalAnswers;

        public AnswerResult(boolean correct, String correctAnswer, String explanation, int xpAwarded,
                int lessonXpEarned, int userTotalXp, int currentStreak, int correctAnswers, int totalAnswers) {
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.xpAwarded = xpAwarded;
            this.lessonXpEarned = lessonXpEarned;
            this.userTotalXp = userTotalXp;
            this.currentStreak = currentStreak;
            this.correctAnswers = correctAnswers;
            this.totalAnswers = totalAnswers;
        }

        public boolean isCorrect() {
            return correct;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public String getExplanation() {
            return explanation;
        }

        /** XP written to the database for this answer (0 when none was due). */
        public int getXpAwarded() {
            return xpAwarded;
        }

        public int getLessonXpEarned() {
            return lessonXpEarned;
        }

        public int getUserTotalXp() {
            return userTotalXp;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalAnswers() {
            return totalAnswers;
        }
    }

    /** Outcome of finishing a lesson attempt, as persisted. */
    public static class LessonCompletionResult {
        private final boolean passed;
        private final boolean practiceMode;
        private final boolean completed;
        private final double scorePercentage;
        private final int correctAnswers;
        private final int totalAnswers;
        private final int lessonXpEarned;
        private final int userTotalXp;

        public LessonCompletionResult(boolean passed, boolean practiceMode, boolean completed, double scorePercentage,
                int correctAnswers, int totalAnswers, int lessonXpEarned, int userTotalXp) {
            this.passed = passed;
            this.practiceMode = practiceMode;
            this.completed = completed;
            this.scorePercentage = scorePercentage;
            this.correctAnswers = correctAnswers;
            this.totalAnswers = totalAnswers;
            this.lessonXpEarned = lessonXpEarned;
            this.userTotalXp = userTotalXp;
        }

        public boolean isPassed() {
            return passed;
        }

        /** True when the lesson had already been completed before this attempt. */
        public boolean isPracticeMode() {
            return practiceMode;
        }

        public boolean isCompleted() {
            return completed;
        }

        public double getScorePercentage() {
            return scorePercentage;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalAnswers() {
            return totalAnswers;
        }

        /** Total XP stored for this lesson. */
        public int getLessonXpEarned() {
            return lessonXpEarned;
        }

        public int getUserTotalXp() {
            return userTotalXp;
        }
    }
}
