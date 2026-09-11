package com.germanlearning.service;

import com.germanlearning.model.ActivityAttempt;
import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.Progress;
import com.germanlearning.model.Skill;
import com.germanlearning.model.User;
import com.germanlearning.repository.ActivityAttemptRepository;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import com.germanlearning.service.activity.GradingResult;
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
 *
 * Phases decide what an answer means. LEARN activities are never submitted
 * here. PRACTICE answers earn XP and feed the lesson score, but cannot fail the
 * lesson. CHECKPOINT answers additionally feed the checkpoint score, which is
 * what completion and unlocking depend on.
 */
@Service
@Transactional
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ActivityAttemptRepository attemptRepository;
    private final ActivityService activityService;
    private final ScoreService scoreService;
    private final UserService userService;
    private final VocabularyService vocabularyService;

    public ProgressService(ProgressRepository progressRepository,
            UserRepository userRepository,
            LessonRepository lessonRepository,
            ActivityAttemptRepository attemptRepository,
            ActivityService activityService,
            ScoreService scoreService,
            UserService userService,
            VocabularyService vocabularyService) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.attemptRepository = attemptRepository;
        this.activityService = activityService;
        this.scoreService = scoreService;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
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
     * before (XP, completion, best score, activities that already paid out) is
     * kept.
     *
     * This is also the moment the learner is counted as active today, which is
     * what their daily streak is built from.
     */
    public Progress startLessonAttempt(Long userId, Long lessonId) {
        userService.updateStreak(userId);

        Progress progress = getOrCreateProgress(userId, lessonId);
        progress.setCorrectAnswers(0);
        progress.setTotalAnswers(0);
        progress.setCheckpointCorrectAnswers(0);
        progress.setCheckpointTotalAnswers(0);
        progress.resetStreak();
        progress.setLastAttemptAt(LocalDateTime.now());
        return progressRepository.saveAndFlush(progress);
    }

    /**
     * The single entry point for answering an activity: grades the answer,
     * updates the streak, awards XP once per activity, records the attempt and
     * persists everything.
     *
     * @return the persisted outcome, including the XP actually stored
     */
    public AnswerResult submitAnswer(Long userId, Long lessonId, Long activityId, String answer) {
        LessonActivity activity = activityService.getActivity(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (activity.getLesson() == null || !activity.getLesson().getId().equals(lessonId)) {
            throw new IllegalArgumentException("Activity " + activityId + " does not belong to lesson " + lessonId);
        }
        if (!activity.isGraded()) {
            throw new IllegalArgumentException("Activity " + activityId + " is not graded");
        }

        GradingResult grading = activityService.grade(activity, answer);
        boolean correct = grading.correct();

        Progress progress = getOrCreateProgress(userId, lessonId);
        progress.incrementTotalAnswers();

        if (correct) {
            progress.incrementCorrectAnswers();
            progress.incrementStreak();
        } else {
            progress.resetStreak();
            // A word the learner just got wrong is exactly the one worth
            // reviewing later, so it files itself in their word bank
            vocabularyService.captureMistake(userId, activity, answer);
        }

        // Only the checkpoint decides completion
        if (activity.getPhase() == ActivityPhase.CHECKPOINT) {
            progress.recordCheckpointAnswer(correct);
        }

        int xpAwarded = 0;
        if (correct && progress.isXpPayableFor(activityId)) {
            xpAwarded = scoreService.calculateXpForCorrectAnswer(
                    activity.getXpReward(), progress.getCurrentStreak());

            progress.addXp(xpAwarded);
            progress.markXpAwardedFor(activityId);

            User user = progress.getUser();
            user.addXp(xpAwarded);
            userRepository.saveAndFlush(user);
        }

        progress.setLastAttemptAt(LocalDateTime.now());
        Progress saved = progressRepository.saveAndFlush(progress);

        attemptRepository.save(
                ActivityAttempt.of(saved.getUser(), activity, answer, correct, xpAwarded));

        return new AnswerResult(
                correct,
                grading.expectedAnswer(),
                grading.feedback(),
                activity.getExplanation(),
                activity.getHint(),
                activity.getPhase(),
                activity.getSkill(),
                activity.getCefrLevel(),
                activity.getTopic(),
                xpAwarded,
                saved.getXpEarned(),
                saved.getUser().getTotalXp(),
                saved.getCurrentStreak(),
                saved.getCorrectAnswers(),
                saved.getTotalAnswers(),
                saved.getCheckpointCorrectAnswers(),
                saved.getCheckpointTotalAnswers());
    }

    /**
     * Finishes an attempt. A lesson with a checkpoint is completed when the
     * checkpoint reaches the threshold; a lesson without one falls back to the
     * overall score, which is how lessons behaved before phases existed.
     * Passing marks the lesson completed (which unlocks the next one) and locks
     * XP so later practice runs pay nothing.
     */
    public LessonCompletionResult completeLesson(Long userId, Long lessonId) {
        Progress progress = getOrCreateProgress(userId, lessonId);

        boolean practiceMode = progress.isCompleted();
        boolean hasCheckpoint = activityService.countCheckpointActivities(lessonId) > 0;

        boolean passed = hasCheckpoint ? progress.hasPassedCheckpoint() : progress.hasPassedThreshold();
        double decidingScore = hasCheckpoint
                ? progress.getCheckpointScorePercentage()
                : progress.getScorePercentage();

        if (passed) {
            progress.setCompleted(true);
            progress.setXpLocked(true); // Lock XP after passing
            progress.updateBestScore(decidingScore); // Track best score
            progress.incrementAttempts();
        }

        Progress saved = progressRepository.saveAndFlush(progress);

        return new LessonCompletionResult(
                passed,
                practiceMode,
                saved.isCompleted(),
                hasCheckpoint,
                decidingScore,
                saved.getCorrectAnswers(),
                saved.getTotalAnswers(),
                saved.getCheckpointCorrectAnswers(),
                saved.getCheckpointTotalAnswers(),
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
        private final String feedback;
        private final String explanation;
        private final String hint;
        private final ActivityPhase phase;
        private final Skill skill;
        private final CefrLevel cefrLevel;
        private final String topic;
        private final int xpAwarded;
        private final int lessonXpEarned;
        private final int userTotalXp;
        private final int currentStreak;
        private final int correctAnswers;
        private final int totalAnswers;
        private final int checkpointCorrectAnswers;
        private final int checkpointTotalAnswers;

        public AnswerResult(boolean correct, String correctAnswer, String feedback, String explanation,
                String hint, ActivityPhase phase, Skill skill, CefrLevel cefrLevel, String topic,
                int xpAwarded, int lessonXpEarned, int userTotalXp, int currentStreak,
                int correctAnswers, int totalAnswers,
                int checkpointCorrectAnswers, int checkpointTotalAnswers) {
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.feedback = feedback;
            this.explanation = explanation;
            this.hint = hint;
            this.phase = phase;
            this.skill = skill;
            this.cefrLevel = cefrLevel;
            this.topic = topic;
            this.xpAwarded = xpAwarded;
            this.lessonXpEarned = lessonXpEarned;
            this.userTotalXp = userTotalXp;
            this.currentStreak = currentStreak;
            this.correctAnswers = correctAnswers;
            this.totalAnswers = totalAnswers;
            this.checkpointCorrectAnswers = checkpointCorrectAnswers;
            this.checkpointTotalAnswers = checkpointTotalAnswers;
        }

        public boolean isCorrect() {
            return correct;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        /** Why the answer was right or wrong, in words a learner can act on. */
        public String getFeedback() {
            return feedback;
        }

        public String getExplanation() {
            return explanation;
        }

        public String getHint() {
            return hint;
        }

        public ActivityPhase getPhase() {
            return phase;
        }

        public Skill getSkill() {
            return skill;
        }

        public CefrLevel getCefrLevel() {
            return cefrLevel;
        }

        public String getTopic() {
            return topic;
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

        public int getCheckpointCorrectAnswers() {
            return checkpointCorrectAnswers;
        }

        public int getCheckpointTotalAnswers() {
            return checkpointTotalAnswers;
        }
    }

    /** Outcome of finishing a lesson attempt, as persisted. */
    public static class LessonCompletionResult {
        private final boolean passed;
        private final boolean practiceMode;
        private final boolean completed;
        private final boolean hasCheckpoint;
        private final double scorePercentage;
        private final int correctAnswers;
        private final int totalAnswers;
        private final int checkpointCorrectAnswers;
        private final int checkpointTotalAnswers;
        private final int lessonXpEarned;
        private final int userTotalXp;

        public LessonCompletionResult(boolean passed, boolean practiceMode, boolean completed,
                boolean hasCheckpoint, double scorePercentage,
                int correctAnswers, int totalAnswers,
                int checkpointCorrectAnswers, int checkpointTotalAnswers,
                int lessonXpEarned, int userTotalXp) {
            this.passed = passed;
            this.practiceMode = practiceMode;
            this.completed = completed;
            this.hasCheckpoint = hasCheckpoint;
            this.scorePercentage = scorePercentage;
            this.correctAnswers = correctAnswers;
            this.totalAnswers = totalAnswers;
            this.checkpointCorrectAnswers = checkpointCorrectAnswers;
            this.checkpointTotalAnswers = checkpointTotalAnswers;
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

        public boolean isHasCheckpoint() {
            return hasCheckpoint;
        }

        /** The score that decided the attempt: checkpoint score when there is one. */
        public double getScorePercentage() {
            return scorePercentage;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalAnswers() {
            return totalAnswers;
        }

        public int getCheckpointCorrectAnswers() {
            return checkpointCorrectAnswers;
        }

        public int getCheckpointTotalAnswers() {
            return checkpointTotalAnswers;
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
