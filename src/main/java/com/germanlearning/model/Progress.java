package com.germanlearning.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "progress")
public class Progress {

    /** Score needed to complete a lesson and unlock the next one. */
    public static final double PASS_THRESHOLD_PERCENTAGE = 80.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private int correctAnswers = 0;

    @Column(nullable = false)
    private int totalAnswers = 0;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private int xpEarned = 0;

    @Column(nullable = false)
    private boolean xpLocked = false;

    @Column(nullable = false)
    private double bestScore = 0.0;

    /**
     * Consecutive correct answers within the current lesson attempt.
     * The explicit default keeps the SQLite ALTER TABLE valid when this column
     * is added to a database that already holds progress rows.
     */
    @Column(name = "current_streak", columnDefinition = "integer not null default 0")
    private int currentStreak = 0;

    /**
     * Answers given in the checkpoint phase. These decide completion, so a
     * mistake made while practising cannot fail the lesson.
     */
    @Column(name = "checkpoint_correct_answers", columnDefinition = "integer not null default 0")
    private int checkpointCorrectAnswers = 0;

    @Column(name = "checkpoint_total_answers", columnDefinition = "integer not null default 0")
    private int checkpointTotalAnswers = 0;

    /**
     * Ids of the activities that have already paid out XP for this lesson,
     * stored comma separated. Used to make XP awarding idempotent per activity
     * so retrying a lesson cannot farm unlimited XP.
     */
    @Column(name = "xp_awarded_activity_ids", length = 2000)
    private String xpAwardedActivityIds;

    private LocalDateTime completedAt;

    private LocalDateTime lastAttemptAt;

    public Progress() {
    }

    public Progress(User user, Lesson lesson) {
        this.user = user;
        this.lesson = lesson;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public void incrementTotalAnswers() {
        this.totalAnswers++;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public void addXp(int xp) {
        this.xpEarned += xp;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public double getScorePercentage() {
        if (totalAnswers == 0) {
            return 0.0;
        }
        return (double) correctAnswers / totalAnswers * 100.0;
    }

    public boolean hasPassedThreshold() {
        return getScorePercentage() >= PASS_THRESHOLD_PERCENTAGE;
    }

    public boolean isXpLocked() {
        return xpLocked;
    }

    public void setXpLocked(boolean xpLocked) {
        this.xpLocked = xpLocked;
    }

    public double getBestScore() {
        return bestScore;
    }

    public void setBestScore(double bestScore) {
        this.bestScore = bestScore;
    }

    public void updateBestScore() {
        updateBestScore(getScorePercentage());
    }

    /** Records the score that decided the attempt, when it beats the old best. */
    public void updateBestScore(double score) {
        if (score > this.bestScore) {
            this.bestScore = score;
        }
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public void incrementStreak() {
        this.currentStreak++;
    }

    public void resetStreak() {
        this.currentStreak = 0;
    }

    public String getXpAwardedActivityIds() {
        return xpAwardedActivityIds;
    }

    public void setXpAwardedActivityIds(String xpAwardedActivityIds) {
        this.xpAwardedActivityIds = xpAwardedActivityIds;
    }

    public Set<Long> getXpAwardedActivities() {
        if (xpAwardedActivityIds == null || xpAwardedActivityIds.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(xpAwardedActivityIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean hasXpBeenAwardedFor(Long activityId) {
        return activityId != null && getXpAwardedActivities().contains(activityId);
    }

    public void markXpAwardedFor(Long activityId) {
        if (activityId == null) {
            return;
        }
        Set<Long> awarded = getXpAwardedActivities();
        if (awarded.add(activityId)) {
            this.xpAwardedActivityIds = awarded.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    /**
     * XP is payable for this activity only if the lesson has not been locked by
     * a previous completion and the activity has not paid out before.
     */
    public boolean isXpPayableFor(Long activityId) {
        return !xpLocked && !hasXpBeenAwardedFor(activityId);
    }

    public int getCheckpointCorrectAnswers() {
        return checkpointCorrectAnswers;
    }

    public void setCheckpointCorrectAnswers(int checkpointCorrectAnswers) {
        this.checkpointCorrectAnswers = checkpointCorrectAnswers;
    }

    public int getCheckpointTotalAnswers() {
        return checkpointTotalAnswers;
    }

    public void setCheckpointTotalAnswers(int checkpointTotalAnswers) {
        this.checkpointTotalAnswers = checkpointTotalAnswers;
    }

    public void recordCheckpointAnswer(boolean correct) {
        this.checkpointTotalAnswers++;
        if (correct) {
            this.checkpointCorrectAnswers++;
        }
    }

    public double getCheckpointScorePercentage() {
        if (checkpointTotalAnswers == 0) {
            return 0.0;
        }
        return (double) checkpointCorrectAnswers / checkpointTotalAnswers * 100.0;
    }

    /**
     * Completion rule for a lesson that has a checkpoint: the checkpoint must
     * have been attempted and must reach the same threshold the lesson score
     * always had to reach.
     */
    public boolean hasPassedCheckpoint() {
        return checkpointTotalAnswers > 0
                && getCheckpointScorePercentage() >= PASS_THRESHOLD_PERCENTAGE;
    }
}
